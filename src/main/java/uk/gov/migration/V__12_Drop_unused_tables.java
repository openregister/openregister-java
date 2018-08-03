package uk.gov.migration;

import org.flywaydb.core.api.migration.MigrationChecksumProvider;
import org.flywaydb.core.api.migration.jdbc.BaseJdbcMigration;

import java.sql.Connection;
import java.sql.Statement;

public class V__12_Drop_unused_tables extends BaseJdbcMigration implements MigrationChecksumProvider {
    @Override
    public void migrate(Connection connection) throws Exception {
        Statement stmt = connection.createStatement();
        try {
            stmt.execute("drop table total_records");
            stmt.execute("drop table current_keys");
        } finally {
            stmt.close();
        }
    }

    @Override
    public Integer getChecksum() {
        return 1;
    }
}
