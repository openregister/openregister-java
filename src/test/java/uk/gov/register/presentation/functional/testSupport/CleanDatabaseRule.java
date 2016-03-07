package uk.gov.register.presentation.functional.testSupport;

import org.junit.rules.ExternalResource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

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
    public void before() throws Throwable {
        try (Connection connection = DriverManager.getConnection(pgUrl, pgUser, "")) {
            try(Statement statement = connection.createStatement()){
                statement.execute("DROP TABLE IF EXISTS " + tableName);
                statement.execute("CREATE TABLE " + tableName + " (SERIAL_NUMBER INTEGER PRIMARY KEY, ENTRY JSONB)");

                statement.execute("DROP TABLE IF EXISTS total_entries");
                statement.execute("CREATE TABLE IF NOT EXISTS   total_entries   (count INTEGER, last_updated TIMESTAMP WITHOUT TIME ZONE DEFAULT now())");
                statement.execute("INSERT INTO TOTAL_ENTRIES(COUNT) VALUES(0)");

                statement.execute("DROP TABLE IF EXISTS CURRENT_KEYS");
                statement.execute("CREATE TABLE CURRENT_KEYS (SERIAL_NUMBER INTEGER PRIMARY KEY, KEY VARCHAR UNIQUE)");

                statement.execute("DROP TABLE IF EXISTS TOTAL_RECORDS");
                statement.execute("CREATE TABLE TOTAL_RECORDS (COUNT INTEGER)");
                statement.execute("INSERT INTO TOTAL_RECORDS(COUNT) VALUES(0)");

                statement.execute("DROP TABLE IF EXISTS STH");
                statement.execute("create table sth (tree_size integer, timestamp bigint, tree_head_signature varchar, sha256_root_hash varchar)");
            }
        }
    }
}
