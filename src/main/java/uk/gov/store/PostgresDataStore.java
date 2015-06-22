package uk.gov.store;

import java.sql.*;

public class PostgresDataStore implements DataStore {

    private final Connection connection;
    private final String table = " STORE ";

    public PostgresDataStore(String pgConnectionString) {
        try {
            connection = DriverManager.getConnection(pgConnectionString);
            try(Statement statement = connection.createStatement()){
                statement.execute("CREATE TABLE IF NOT EXISTS " + table+ "(content bytea)");
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Override
    public void add(byte[] message) {
        try {
            try(PreparedStatement statement = connection.prepareStatement("INSERT INTO"  +table + " values(?)")){
                statement.setBytes(1, message);
                statement.execute();
            }
        } catch (SQLException e) {
            //TODO:
            throw new RuntimeException(e);
        }

    }
}
