package uk.gov.store;

import com.google.common.base.Throwables;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;

public class LogStream {
    private final Producer<String, byte[]> producer;
    public static final String TOPIC_NAME = "register";
    private final HighWaterMarkDAO highWaterMarkDAO;
    private final EntriesQueryDAO entriesQueryDAO;

    public LogStream(HighWaterMarkDAO highWaterMarkDAO, EntriesQueryDAO entriesQueryDAO, KafkaProducer<String, byte[]> kafkaProducer) {
        this.highWaterMarkDAO = highWaterMarkDAO;
        this.entriesQueryDAO = entriesQueryDAO;
        this.producer = kafkaProducer;

        highWaterMarkDAO.ensureTableExists();
    }

    public void notifyOfNewEntries() {
        sendNewEntriesToKafka();
    }

    private void send(IndexedEntry entry) {
        producer.send(new ProducerRecord<>(TOPIC_NAME, String.valueOf(entry.getSerial()), entry.getEntry()));
    }

    public void close() throws IOException {
        producer.close();
        entriesQueryDAO.close();
        highWaterMarkDAO.close();
    }

    private void sendNewEntriesToKafka() {
        try {
            int oldHighWaterMark = highWaterMarkDAO.getCurrentHighWaterMark();

            sendEntriesToKafkaSince(oldHighWaterMark);
        } catch (SQLException e) {
            throw Throwables.propagate(e);
        }
    }

    private void sendEntriesToKafkaSince(int oldHighWaterMark) throws SQLException {
        int newHighWaterMark = oldHighWaterMark;

        // send all entries since old high water mark
        Iterator<IndexedEntry> entries = entriesQueryDAO.getEntriesSince(oldHighWaterMark);
        while (entries.hasNext()) {
            IndexedEntry entry = entries.next();
            send(entry);
            newHighWaterMark = Math.max(newHighWaterMark, entry.getSerial());
        }

        // if we actually sent anything new, we need to record that we did
        if (oldHighWaterMark != newHighWaterMark) {
            // update high water mark so we don't send these entries again
            // if this fails, we may send the same entry twice, but that's okay
            // because it's identified by serial number so the consumer can deduplicate
            highWaterMarkDAO.updateHighWaterMark(newHighWaterMark);
        }
    }
}
