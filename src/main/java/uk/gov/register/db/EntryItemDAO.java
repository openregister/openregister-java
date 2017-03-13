package uk.gov.register.db;

import org.skife.jdbi.v2.sqlobject.SqlBatch;
import org.skife.jdbi.v2.sqlobject.customizers.BatchChunkSize;
import uk.gov.register.core.Entry;
import uk.gov.register.store.postgres.BindEntry;

public interface EntryItemDAO {
    @SqlBatch("insert into entry_item(entry_number, sha256hex) values (:entry_number, :sha256hex)")
    @BatchChunkSize(1000)
    void insertInBatch(@BindEntry Iterable<Entry> entries);
}
