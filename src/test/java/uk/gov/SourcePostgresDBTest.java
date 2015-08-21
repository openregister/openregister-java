package uk.gov;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.*;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public class SourcePostgresDBTest {
    String connectionString = "jdbc:postgresql://localhost:5432/postgres";

    String entriesTableName = "entries";

    Connection connection;


    SourcePostgresDB sourcePostgresDB;


    @Before
    public void setup() throws SQLException {
        connection = DriverManager.getConnection(connectionString);
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS " + entriesTableName + " (ID SERIAL PRIMARY KEY, ENTRY BYTEA)");
            statement.execute("DELETE FROM " + entriesTableName);
        }
        sourcePostgresDB = new SourcePostgresDB(connectionString);
    }

    @After
    public void cleanup() throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE " + entriesTableName);
        }
        connection.close();

    }

    @Test
    public void read_returnsAllTheEntries_givenCurrentWaterMarkIsZero() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("INSERT INTO " + entriesTableName + "(ENTRY) VALUES ('abc'), ('def'), ('fgh')");
        }

        ResultSet result = sourcePostgresDB.read(0);
        int noOfRows = 0;
        while(result.next()){
            noOfRows++;
        }
        assertThat(noOfRows, equalTo(3));
    }

    @Test
    public void read_returnsAllTheEntriesAfterWatermark() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("INSERT INTO " + entriesTableName + "(ENTRY) VALUES ('abc'), ('def'), ('fgh')");
        }

        ResultSet result = sourcePostgresDB.read(2);

        result.next();
        assertThat(new String(result.getBytes("ENTRY")), equalTo("fgh"));
        assertFalse(result.next());
    }
}
