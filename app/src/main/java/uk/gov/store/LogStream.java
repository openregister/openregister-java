package uk.gov.store;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class LogStream {
    public static final String HIGH_WATER_MARK_TABLE = "streamed_entries";
    private final String entryTable;
    private final Producer<String, byte[]> producer;
    public static final String TOPIC_NAME = "register";
    private final Connection conn;

    public LogStream(String pgConnectionString, String storeName, String kafkaBootstrapServers) {
        this.producer = new KafkaProducer<>(ImmutableMap.of(
                "bootstrap.servers", kafkaBootstrapServers,
                "key.serializer", "org.apache.kafka.common.serialization.StringSerializer",
                "value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer"
        ));

        this.entryTable = storeName + "_STORE";

        try {
            conn = DriverManager.getConnection(pgConnectionString);

            try (Statement statement = conn.createStatement()) {
                statement.execute("CREATE TABLE IF NOT EXISTS " + HIGH_WATER_MARK_TABLE + " (ID INTEGER PRIMARY KEY, TIME TIMESTAMP)");
            }
        } catch (SQLException e) {
            throw Throwables.propagate(e);
        }
    }

    public void notifyOfNewEntries() {
        sendNewEntriesToKafka();
    }

    public void send(int serial, byte[] message) {
        producer.send(new ProducerRecord<>(TOPIC_NAME, String.valueOf(serial), message));
    }

    public void close() throws IOException {
        producer.close();
    }

    private void sendNewEntriesToKafka() {
        try {
            int oldHighWaterMark = getPreviousHighWaterMark();

            sendEntriesToKafkaSince(oldHighWaterMark);
        } catch (SQLException e) {
            throw Throwables.propagate(e);
        }
    }

    private void sendEntriesToKafkaSince(int oldHighWaterMark) throws SQLException {
        int newHighWaterMark = oldHighWaterMark;

        // send all entries since old high water mark
        try (PreparedStatement stmt = conn.prepareStatement("SELECT ID,ENTRY FROM " + entryTable + " WHERE ID > ? ORDER BY ID")) {
            stmt.setInt(1, oldHighWaterMark);
            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    send(resultSet.getInt("id"), resultSet.getBytes("entry"));
                    newHighWaterMark = Math.max(newHighWaterMark, resultSet.getInt("id"));
                }
            }
        }

        // if we actually sent anything new, we need to record that we did
        if (oldHighWaterMark != newHighWaterMark) {
            // update high water mark so we don't send these entries again
            // if this fails, we may send the same entry twice, but that's okay
            // because it's identified by serial number so the consumer can deduplicate
            try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO " + HIGH_WATER_MARK_TABLE + " VALUES(?,now())")) {
                stmt.setInt(1, newHighWaterMark);
                stmt.execute();
            }
        }
    }

    private int getPreviousHighWaterMark() throws SQLException {
        int highWaterMark;
        try (PreparedStatement stmt = conn.prepareStatement("SELECT ID FROM " + HIGH_WATER_MARK_TABLE + " ORDER BY ID DESC LIMIT 1")) {
            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    highWaterMark = resultSet.getInt("ID");
                } else {
                    // we've never sent anything
                    highWaterMark = -1;
                }
            }
        }
        return highWaterMark;
    }
}
