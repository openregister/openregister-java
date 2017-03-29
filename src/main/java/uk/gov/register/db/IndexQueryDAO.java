package uk.gov.register.db;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;

public interface IndexQueryDAO {
    @SqlQuery("select max(r.index_entry_number) from (select greatest(start_index_entry_number, end_index_entry_number) as index_entry_number from index where name = :name) r")
    int getCurrentIndexEntryNumber(@Bind("name") String indexName);

    @SqlQuery("select count(1) from index where name = :name and key = :key and sha256hex = :sha256hex and end_entry_number is null")
    int getExistingIndexCountForItem(@Bind("name") String indexName, @Bind("key") String key, @Bind("sha256hex") String sha256hex);
}
