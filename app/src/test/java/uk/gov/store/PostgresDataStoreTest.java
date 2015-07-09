package uk.gov.store;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.*;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class PostgresDataStoreTest {

    private String testPostgresConnectionUri = "jdbc:postgresql://localhost:5432/test_mint";
    private String tableName = EntriesQueryDAO.tableName;
    private Connection connection;

    @Before
    public void setup() throws SQLException, ClassNotFoundException {
        connection = DriverManager.getConnection(testPostgresConnectionUri);
    }

    @After
    public void cleanup() throws SQLException {
        connection.createStatement().execute("DROP TABLE " + tableName);
        connection.close();
    }

    @Test
    public void add_savesTheMessageToTheStoreWithSequenceNumber() throws SQLException {
        PostgresDataStore postgresDataStore = new PostgresDataStore(testPostgresConnectionUri);
        assertFalse(records().next());

        byte[] message1 = {1, 2, 3};
        byte[] message2 = {4, 5, 6};
        postgresDataStore.add(message1);
        postgresDataStore.add(message2);

        ResultSet result = records();

        assertTrue(result.next());
        assertThat(result.getInt("id"), is(1));
        assertThat(result.getBytes("entry"), is(message1));
        assertTrue(result.next());
        assertThat(result.getInt("id"), is(2));
        assertThat(result.getBytes("entry"), is(message2));
    }

    private ResultSet records() throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute("SELECT * FROM " + tableName);
        return statement.getResultSet();
    }
}
