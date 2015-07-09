package uk.gov.store;

import java.sql.*;

public class PostgresDataStore implements DataStore {

    private final Connection connection;
    private final String table;

    public PostgresDataStore(String connectionString) {
        try {
            table = EntriesQueryDAO.tableName;
            connection = DriverManager.getConnection(connectionString);
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE IF NOT EXISTS " + table + " (ID SERIAL PRIMARY KEY, ENTRY BYTEA)");
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Override
    public void add(byte[] message) {
        try {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO " + table + "(ENTRY) values(?)")) {
                statement.setBytes(1, message);
                statement.execute();
            }
        } catch (SQLException e) {
            //TODO:
            throw new RuntimeException(e);
        }

    }

    @Override
    public void close() throws SQLException {
        connection.close();
    }
}
