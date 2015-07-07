package uk.gov.register.presentation.functional;

import org.junit.rules.ExternalResource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class CleanDatabaseRule extends ExternalResource {
    private final String tableName;
    private final String pgUrl;

    public CleanDatabaseRule(String pgUrl, String tableName) {
        this.tableName = tableName;
        this.pgUrl = pgUrl;
    }

    @Override
    protected void before() throws Throwable {
        Connection connection = DriverManager.getConnection(pgUrl);
        try (Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS " + tableName);
        }
    }
}
