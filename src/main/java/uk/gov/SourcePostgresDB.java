package uk.gov;

import java.sql.ResultSet;
import java.sql.SQLException;

class SourcePostgresDB extends PostgresDB {

    private final String tableName;

    public SourcePostgresDB(String registerName, String connectionString) throws SQLException {
        super(connectionString);
        this.tableName = registerName + "_entries";
    }

    public ResultSet read() throws SQLException {
        return connection.prepareStatement("SELECT ENTRY FROM " + tableName).executeQuery();
    }

}
