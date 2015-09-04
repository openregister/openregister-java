package uk.gov;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.*;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class DestinationPostgresDBTest {
    String connectionString = "jdbc:postgresql://localhost:5432/postgres";

    String waterMarkTableName = "streamed_entries";

    Connection connection;
    Statement statement;

    DestinationPostgresDB destinationPostgresDB;

    @Before
    public void setup() throws SQLException {
        connection = DriverManager.getConnection(connectionString);
        destinationPostgresDB = new DestinationPostgresDB("register", connectionString);
        statement = connection.createStatement();
    }

    @After
    public void cleanup() throws SQLException {
        statement.execute("DROP TABLE " + waterMarkTableName);
        statement.close();
        connection.close();
    }

    @Test
    public void constructor_createsWaterMarkTableAndInsertsDefaultValueAsZero_WhenThereIsNoWaterMarkAvailable() throws SQLException {
        ResultSet resultSet = statement.executeQuery("SELECT ID FROM " + waterMarkTableName);
        assertThat(resultSet.next(), equalTo(true));
        assertThat(resultSet.getInt("ID"), equalTo(0));
    }

    @Test
    public void constructor_doesNotSetWaterMarkValueZero_whenTableAlreadyContainsWaterMark() throws SQLException {
        statement.executeUpdate("UPDATE " + waterMarkTableName + " set ID=5");

        new DestinationPostgresDB("register", connectionString);

        ResultSet resultSet = statement.executeQuery("SELECT ID FROM " + waterMarkTableName);
        assertThat(resultSet.next(), equalTo(true));
        assertThat(resultSet.getInt("ID"), equalTo(5));

    }

    @Test
    public void currentWaterMark_returnsZeroWhenNoEntryInTheWaterMarkTable() throws SQLException {
        int currentWaterMark = destinationPostgresDB.currentWaterMark();
        assertThat(currentWaterMark, equalTo(0));
    }

    @Test
    public void currentWaterMark_returnsTheCurrentWaterMarkEntry() throws SQLException {
        statement.executeUpdate("UPDATE " + waterMarkTableName + " SET ID=5");

        int currentWaterMark = destinationPostgresDB.currentWaterMark();
        assertThat(currentWaterMark, equalTo(5));
    }
}
