package uk.gov;

import java.sql.*;

abstract class PostgresDB {

    private final String connectionString;
    protected final Connection connection;

    public PostgresDB(String connectionString) throws SQLException {
        this.connectionString = connectionString;
        connection = DriverManager.getConnection(connectionString);
    }

    public void closeConnection() {
        ConsoleLogger.log("Closing connection: " + connectionString);
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

