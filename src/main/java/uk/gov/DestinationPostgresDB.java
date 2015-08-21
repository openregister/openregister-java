package uk.gov;

import org.postgresql.util.PGobject;

import java.nio.charset.Charset;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

class DestinationPostgresDB extends PostgresDB {

    private final String tableName;

    public DestinationPostgresDB(String registerName, String connectionString) throws SQLException {
        super(connectionString);
        this.tableName = registerName + "_ordered_entry_index";
    }

    public void write(ResultSet resultSet) throws SQLException {
        while (resultSet.next()) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO " + tableName + "(ENTRY) VALUES(?)")) {
                statement.setObject(1, jsonbObject(resultSet.getBytes("ENTRY")));
                statement.executeUpdate();
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
