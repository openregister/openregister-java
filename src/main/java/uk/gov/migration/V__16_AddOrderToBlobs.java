package uk.gov.migration;

import org.flywaydb.core.api.migration.MigrationChecksumProvider;
import org.flywaydb.core.api.migration.jdbc.BaseJdbcMigration;

import java.sql.Connection;
import java.sql.Statement;

public class V__16_AddOrderToBlobs extends BaseJdbcMigration implements MigrationChecksumProvider {
    @Override
    public void migrate(Connection connection) throws Exception {
        Statement stmt = connection.createStatement();

        try {
            stmt.execute("alter table item add column item_order serial");

            // Work out the order of an item using the entries and system entries that reference it
            // since the two logs are independent, smoosh them together and treat the system entries
            // as coming first.
            stmt.execute("" +
                    "with all_entry as (\n" +
                    "  select * from entry union all select * from entry_system\n" +
                    ")\n" +
                    ", combined_log as (\n" +
                    "  select sha256hex, row_number() over(order by type::text,entry_number) as combined_entry_number, row_number() over(partition by sha256hex order by type::text, entry_number) as item_entry_number from all_entry\n" +
                    ")\n" +
                    ", first_snapshot as (\n" +
                    "  select sha256hex, combined_entry_number from combined_log where item_entry_number=1\n" +
                    ")\n" +
                    "\n" +
                    "update item set item_order=first_snapshot.combined_entry_number from first_snapshot where item.sha256hex=first_snapshot.sha256hex");

            stmt.execute("alter table item alter column item_order set not null");

        } finally {
            stmt.close();
        }
    }


    @Override
    public Integer getChecksum() {
        return 1;
    }
}
