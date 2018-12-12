package uk.gov.migration;

import org.flywaydb.core.api.migration.MigrationChecksumProvider;
import org.flywaydb.core.api.migration.jdbc.BaseJdbcMigration;

import java.sql.Connection;
import java.sql.Statement;

public class V__17_RenameItemOrderToBlobOrder extends BaseJdbcMigration implements MigrationChecksumProvider {
    @Override
    public void migrate(Connection connection) throws Exception {
        Statement stmt = connection.createStatement();

        try {
            stmt.execute("alter table item rename column item_order to blob_order");
            stmt.execute("create unique index on item(blob_order)");
        } finally {
            stmt.close();
        }
    }


    @Override
    public Integer getChecksum() {
        return 1;
    }
}
