package uk.gov.migration;

import org.flywaydb.core.api.migration.MigrationChecksumProvider;
import org.flywaydb.core.api.migration.jdbc.BaseJdbcMigration;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class V__13_Add_item_to_entry_table extends BaseJdbcMigration implements MigrationChecksumProvider {
    @Override
    public void migrate(Connection connection) throws Exception {
        Statement stmt = connection.createStatement();
        try {
            stmt.execute("alter table entry add column sha256hex varchar");
            stmt.execute("alter table entry_system add column sha256hex varchar");

            ResultSet resultSetUser = stmt.executeQuery("select count(*) as duplicates from entry_item ou where (select count(*) from entry_item inr where inr.entry_number = ou.entry_number) > 1");
            if (resultSetUser.next() && resultSetUser.getInt("duplicates") > 0) {
                throw new RuntimeException("Unexpected multi-item entries exist in entry_item table");
            }

            ResultSet resultSetSystem = stmt.executeQuery("select count(*) as duplicates from entry_item_system ou where (select count(*) from entry_item_system inr where inr.entry_number = ou.entry_number) > 1");
            if (resultSetSystem.next() && resultSetSystem.getInt("duplicates") > 0) {
                throw new RuntimeException("Unexpected multi-item entries exist in entry_item_system table");
            }

            stmt.execute("update entry set sha256hex = ei.sha256hex from (select * from entry_item) ei where entry.entry_number = ei.entry_number");
            stmt.execute("update entry_system set sha256hex = ei.sha256hex from (select * from entry_item_system) ei where entry_system.entry_number = ei.entry_number");
        } finally {
            stmt.close();
        }
    }

    @Override
    public Integer getChecksum() {
        return 1;
    }
}
