package uk.gov.migration;

import org.flywaydb.core.api.migration.MigrationChecksumProvider;
import org.flywaydb.core.api.migration.jdbc.BaseJdbcMigration;

import java.sql.Connection;
import java.sql.Statement;

public class V__18_ResetSequences extends BaseJdbcMigration implements MigrationChecksumProvider {
    @Override
    public void migrate(Connection connection) throws Exception {
        Statement stmt = connection.createStatement();

        try {
            stmt.execute("SELECT setval('item_item_order_seq', coalesce((select max(blob_order)+1 from item), 1), false)");
        } finally {
            stmt.close();
        }
    }


    @Override
    public Integer getChecksum() {
        return 1;
    }
}
