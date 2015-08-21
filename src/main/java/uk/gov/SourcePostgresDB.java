package uk.gov;

import java.sql.ResultSet;
import java.sql.SQLException;

class SourcePostgresDB extends PostgresDB {

    private final String tableName;

    public SourcePostgresDB(String connectionString) throws SQLException {
        super(connectionString);
        this.tableName = "entries";
    }

    public ResultSet read(int currentWaterMark) throws SQLException {
        return connection.prepareStatement("SELECT ENTRY FROM " + tableName + " WHERE ID > " + currentWaterMark).executeQuery();
    }

}
