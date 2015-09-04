package uk.gov;

import java.sql.*;

abstract class PostgresDB {

    private final String register;
    protected final Connection connection;

    public PostgresDB(String register, String connectionString) throws SQLException {
        this.register = register;
        connection = DriverManager.getConnection(connectionString);
    }

    public void closeConnection() {
        ConsoleLogger.log("Closing " + this.getClass().getCanonicalName() + " connection for register " + register);
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

