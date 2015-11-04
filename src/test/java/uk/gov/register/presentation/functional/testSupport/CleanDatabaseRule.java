package uk.gov.register.presentation.functional.testSupport;

import org.junit.rules.ExternalResource;

import java.sql.*;

public class CleanDatabaseRule extends ExternalResource {
    private final String pgUser;
    private final String tableName;
    private final String pgUrl;

    public CleanDatabaseRule(String pgUrl, String pgUser, String tableName) {
        this.pgUser = pgUser;
        this.tableName = tableName;
        this.pgUrl = pgUrl;
    }

    @Override
    protected void before() throws Throwable {
        try (Connection connection = DriverManager.getConnection(pgUrl, pgUser, "")) {
            connection.prepareStatement("DROP TABLE IF EXISTS " + tableName).execute();
            connection.prepareStatement("CREATE TABLE " + tableName + " (SERIAL_NUMBER SERIAL PRIMARY KEY, ENTRY JSONB)").execute();

            connection.prepareStatement("DROP TABLE IF EXISTS TOTAL_ENTRIES").execute();
            connection.prepareStatement("CREATE TABLE TOTAL_ENTRIES (COUNT INTEGER)").execute();
            connection.prepareStatement("INSERT INTO TOTAL_ENTRIES(COUNT) VALUES(0)").execute();

            connection.prepareStatement("DROP TABLE IF EXISTS CURRENT_KEYS").execute();
            connection.prepareStatement("CREATE TABLE CURRENT_KEYS (SERIAL_NUMBER INTEGER PRIMARY KEY, KEY VARCHAR UNIQUE)").execute();

            connection.prepareStatement("DROP TABLE IF EXISTS TOTAL_RECORDS").execute();
            connection.prepareStatement("CREATE TABLE TOTAL_RECORDS (COUNT INTEGER)").execute();
            connection.prepareStatement("INSERT INTO TOTAL_RECORDS(COUNT) VALUES(0)").execute();
        }
    }
}
