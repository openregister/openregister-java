package uk.gov.migration;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.flywaydb.core.api.migration.MigrationChecksumProvider;
import org.flywaydb.core.api.migration.jdbc.BaseJdbcMigration;

public class V__14_New_hashing_algorithm extends BaseJdbcMigration implements MigrationChecksumProvider {
    @Override
    public void migrate(Connection connection) throws Exception {
        Statement stmt = connection.createStatement();
        try {
            stmt.execute("alter table entry add column blob_hash varchar");
            stmt.execute("alter table entry_system add column blob_hash varchar");
            stmt.execute("alter table item add column blob_hash varchar");

            stmt.execute("update entry set blob_hash = sha256hex;");
            stmt.execute("update entry_system set blob_hash = sha256hex;");
            stmt.execute("update item set blob_hash = sha256hex;");

            stmt.execute("alter table item alter column blob_hash set not null");
        } finally {
            stmt.close();
        }
    }

    @Override
    public Integer getChecksum() {
        return 1;
    }
}
