package uk.gov;

import java.sql.*;

abstract class PostgresDB {

    protected final Connection connection;

    public PostgresDB(String connectionString) throws SQLException {
        connection = DriverManager.getConnection(connectionString);
    }

}

