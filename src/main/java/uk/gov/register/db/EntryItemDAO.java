package uk.gov.register.db;

import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlBatch;
import org.skife.jdbi.v2.sqlobject.customizers.BatchChunkSize;
import org.skife.jdbi.v2.sqlobject.customizers.OverrideStatementLocatorWith;
import uk.gov.register.util.EntryItemPair;

@OverrideStatementLocatorWith(SchemaRewriter.class)
public interface EntryItemDAO {
    @SqlBatch("insert into :schema.entry_item(entry_number, sha256hex) values (:entryNumber, :sha256hex)")
    @BatchChunkSize(1000)
    void insertInBatch(@BindBean Iterable<EntryItemPair> entries);
}
