package uk.gov;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class DestinationPostgresDBTest {
    String connectionString = "jdbc:postgresql://localhost:5432/postgres";

    Connection connection;
    String waterMarkTableName = "streamed_entries";

    DestinationPostgresDB destinationPostgresDB;

    @Before
    public void setup() throws SQLException {
        connection = DriverManager.getConnection(connectionString);
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS " + waterMarkTableName + " (ID INTEGER PRIMARY KEY, TIME TIMESTAMP)");
            statement.execute("DELETE FROM " + waterMarkTableName);
        }
        destinationPostgresDB = new DestinationPostgresDB(connectionString);
    }

    @After
    public void cleanup() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE " + waterMarkTableName);
        }
        connection.close();
    }

    @Test
    public void currentWaterMark_returnsZeroWhenNoEntryInTheWaterMarkTable() throws SQLException {
        int currentWaterMark = destinationPostgresDB.currentWaterMark();
        assertThat(currentWaterMark, equalTo(0));
    }

    @Test
    public void currentWaterMark_returnsTheCurrentWaterMarkEntry() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("INSERT INTO " + waterMarkTableName + "(ID, TIME) values(5, now())");
        }

        int currentWaterMark = destinationPostgresDB.currentWaterMark();
        assertThat(currentWaterMark, equalTo(5));
    }
}
