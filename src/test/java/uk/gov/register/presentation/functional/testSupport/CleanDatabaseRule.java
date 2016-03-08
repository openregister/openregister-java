package uk.gov.register.presentation.functional.testSupport;

import org.junit.rules.ExternalResource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class CleanDatabaseRule extends ExternalResource {
    private final String pgUser;
    private final String pgUrl;

    public CleanDatabaseRule(String pgUrl, String pgUser) {
        this.pgUser = pgUser;
        this.pgUrl = pgUrl;
    }

    @Override
    public void before() throws Throwable {
        try (Connection connection = DriverManager.getConnection(pgUrl, pgUser, "")) {
            try(Statement statement = connection.createStatement()){
                statement.execute("DROP TABLE IF EXISTS ordered_entry_index");
                statement.execute("CREATE TABLE ordered_entry_index (SERIAL_NUMBER INTEGER PRIMARY KEY, ENTRY JSONB, leaf_input varchar)");

                statement.execute("DROP TABLE IF EXISTS total_entries");
                statement.execute("CREATE TABLE IF NOT EXISTS   total_entries   (count INTEGER, last_updated TIMESTAMP WITHOUT TIME ZONE DEFAULT now())");
                statement.execute("INSERT INTO TOTAL_ENTRIES(COUNT) VALUES(0)");

                statement.execute("DROP TABLE IF EXISTS CURRENT_KEYS");
                statement.execute("CREATE TABLE CURRENT_KEYS (SERIAL_NUMBER INTEGER PRIMARY KEY, KEY VARCHAR UNIQUE)");

                statement.execute("DROP TABLE IF EXISTS TOTAL_RECORDS");
                statement.execute("CREATE TABLE TOTAL_RECORDS (COUNT INTEGER)");
                statement.execute("INSERT INTO TOTAL_RECORDS(COUNT) VALUES(0)");
            }
        }
    }

}
