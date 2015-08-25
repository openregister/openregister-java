package uk.gov;

import org.postgresql.util.PGobject;

import java.nio.charset.Charset;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

class DestinationPostgresDB extends PostgresDB {
    private final String indexedEntriesTableName;
    private final String waterMarkTableName;

    public DestinationPostgresDB(String connectionString) throws SQLException {
        super(connectionString);
        this.indexedEntriesTableName = "ordered_entry_index";
        this.waterMarkTableName = "streamed_entries";
    }

    public void write(ResultSet resultSet) throws SQLException {
        while (resultSet.next()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO " + indexedEntriesTableName + "(ENTRY) VALUES(?)")) {
                statement.setObject(1, jsonbObject(resultSet.getBytes("ENTRY")));
                statement.executeUpdate();
            }
            try (PreparedStatement statement = connection.prepareStatement("UPDATE " + waterMarkTableName + " SET ID = ID + 1")) {
                statement.executeUpdate();
            }
            connection.setAutoCommit(true);
        }
    }

    public int currentWaterMark() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            try(ResultSet resultSet = statement.executeQuery("SELECT ID FROM " + waterMarkTableName)){
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
