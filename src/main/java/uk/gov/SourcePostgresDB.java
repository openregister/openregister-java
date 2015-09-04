package uk.gov;

import java.sql.ResultSet;
import java.sql.SQLException;

class SourcePostgresDB extends PostgresDB {

    private final String tableName;

    public SourcePostgresDB(String register, String connectionString) throws SQLException {
        super(register, connectionString);
        this.tableName = "entries";
    }

    public ResultSet read(int currentWaterMark) throws SQLException {
        return connection.prepareStatement("SELECT ENTRY FROM " + tableName + " WHERE ID > " + currentWaterMark).executeQuery();
    }

}
