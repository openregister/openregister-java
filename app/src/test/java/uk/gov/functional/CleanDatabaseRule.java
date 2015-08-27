package uk.gov.functional;

import org.junit.rules.ExternalResource;
import uk.gov.store.EntriesUpdateDAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class CleanDatabaseRule extends ExternalResource {
    private final Connection pgConnection;

    public CleanDatabaseRule(String postgresConnectionString) {
        try {
            pgConnection = DriverManager.getConnection(postgresConnectionString, "postgres", "");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void before() throws SQLException {
        try (Statement statement = pgConnection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS " + EntriesUpdateDAO.tableName);
        }
    }
}

