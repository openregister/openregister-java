package uk.gov.store;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.*;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class PostgresDataStoreTest {

    private String testPostgresConnectionUri = "jdbc:postgresql://localhost:5432/test_mint";
    private Connection connection;

    @Before
    public void setup() throws SQLException, ClassNotFoundException {
        connection = DriverManager.getConnection(testPostgresConnectionUri);
    }

    @After
    public void cleanup() throws SQLException {
        connection.createStatement().execute("DELETE FROM STORE");
        connection.close();
    }

    @Test
    public void add_savesTheMessageToTheStore() throws SQLException {
        PostgresDataStore postgresDataStore = new PostgresDataStore(testPostgresConnectionUri);
        assertNull(tableRecord());

        postgresDataStore.add(new byte[]{1, 2, 3});

        assertThat(tableRecord(), is(new byte[]{1, 2, 3}));
    }

    private byte[] tableRecord() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("SELECT * FROM STORE");
            ResultSet resultSet = statement.getResultSet();
            return resultSet.next() ? resultSet.getBytes(1) : null;
        }
    }
}
