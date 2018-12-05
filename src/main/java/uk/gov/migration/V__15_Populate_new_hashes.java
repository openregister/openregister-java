package uk.gov.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.flywaydb.core.api.migration.MigrationChecksumProvider;
import org.flywaydb.core.api.migration.jdbc.BaseJdbcMigration;
import uk.gov.register.util.JsonToBlobHash;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class V__15_Populate_new_hashes extends BaseJdbcMigration implements MigrationChecksumProvider {
    @Override
    public void migrate(Connection connection) throws Exception {
        Statement stmt = connection.createStatement();
        PreparedStatement updateItem = connection.prepareStatement("update item set blob_hash=? where sha256hex = ?");
        PreparedStatement updateEntry = connection.prepareStatement("update entry set blob_hash=? where sha256hex = ?");
        PreparedStatement updateEntrySystem = connection.prepareStatement("update entry_system set blob_hash=? where sha256hex = ?");

        try {
            ResultSet itemResults = stmt.executeQuery("select sha256hex, content from item");
            while(itemResults.next()) {
                String oldHashValue = itemResults.getString(1);
                String content = itemResults.getString(2);
                String newHashValue = JsonToBlobHash.apply(new ObjectMapper().readTree(content)).getValue();

                System.out.println("Updating " + oldHashValue + " -> " + newHashValue);

                updateItem.setString(1, newHashValue);
                updateItem.setString(2, oldHashValue);
                updateEntry.setString(1, newHashValue);
                updateEntry.setString(2, oldHashValue);
                updateEntrySystem.setString(1, newHashValue);
                updateEntrySystem.setString(2, oldHashValue);

                updateItem.executeUpdate();
                updateEntry.executeUpdate();
                updateEntrySystem.executeUpdate();
            }
        } finally {
            stmt.close();
            updateItem.close();;
            updateEntry.close();
            updateEntrySystem.close();
        }
    }


    @Override
    public Integer getChecksum() {
        return 1;
    }
}
