package uk.gov.indexer;

import org.postgresql.util.PGobject;

import java.nio.charset.Charset;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

class DestinationPostgresDB extends PostgresDB {
    private final String indexedEntriesTableName;
    private final String waterMarkTableName;

    public DestinationPostgresDB(String register, String connectionString) throws SQLException {
        super(register, connectionString);
        this.indexedEntriesTableName = "ordered_entry_index";
        this.waterMarkTableName = "streamed_entries";
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + indexedEntriesTableName + " (ID SERIAL PRIMARY KEY, ENTRY JSONB)");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + waterMarkTableName + " (ID INTEGER PRIMARY KEY, TIME TIMESTAMP)");
            if (!statement.executeQuery("SELECT ID FROM " + waterMarkTableName).next()) {
                statement.executeUpdate("INSERT INTO " + waterMarkTableName + "(ID, TIME) VALUES(0, NOW())");
            }
        }
    }

    public void write(ResultSet resultSet) throws SQLException {
        while (resultSet.next()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO " + indexedEntriesTableName + "(ENTRY) VALUES(?)")) {
                statement.setObject(1, jsonbObject(resultSet.getBytes("ENTRY")));
                statement.executeUpdate();
            }
            try (PreparedStatement statement = connection.prepareStatement("UPDATE " + waterMarkTableName + " SET ID = ID + 1, time=now()")) {
                statement.executeUpdate();
            }
            connection.commit();
        }
    }

    public int currentWaterMark() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("SELECT ID FROM " + waterMarkTableName)) {
                return resultSet.next() ? resultSet.getInt("ID") : 0;
            }
        }
    }

    private PGobject jsonbObject(byte[] value) {
        try {
            PGobject pGobject = new PGobject();
            pGobject.setType("jsonb");
            pGobject.setValue(new String(value, Charset.forName("UTF-8")));
            return pGobject;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
