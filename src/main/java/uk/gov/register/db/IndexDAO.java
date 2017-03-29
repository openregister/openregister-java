package uk.gov.register.db;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

public interface IndexDAO {
    @SqlUpdate("insert into index (name, key, sha256hex, start_entry_number, start_index_entry_number) values (:name, :key, :sha256hex, :start_entry_number, :start_index_entry_number)")
    void start(@Bind("name") String indexName, @Bind("key") String key, @Bind("sha256hex") String itemHash, @Bind("start_entry_number") int startEntryNumber, @Bind("start_index_entry_number") int startIndexEntryNumber);

    @SqlUpdate("update index set end_entry_number = :end_entry_number, end_index_entry_number = :end_index_entry_number where name = :name and key = :key and sha256hex = :sha256hex")
    void end(@Bind("name") String indexName, @Bind("key") String key, @Bind("sha256hex") String itemHash, @Bind("end_entry_number") int endEntryNumber, @Bind("end_index_entry_number") int endIndexEntryNumber);
}
