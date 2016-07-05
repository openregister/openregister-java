package uk.gov.indexer.dao;

import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;

import java.util.List;

@UseStringTemplate3StatementLocator("/sql/init_entry.sql")
public interface EntryUpdateDAO extends DBConnectionDAO {

    @SqlUpdate
    void ensureEntrySchemaInPlace();

    @SqlQuery("SELECT MAX(entry_number) FROM entry")
    int lastReadEntryNumber();

    @SqlBatch("INSERT INTO entry(entry_number, sha256hex, timestamp) VALUES(:entryNumber, :itemHash, :timestamp)")
    void writeBatch(@BindBean Iterable<Entry> entries);

    @SqlUpdate("update total_entries set count=count+:entriesWritten")
    void updateTotalEntries(@Bind("entriesWritten") long entriesWritten);

    @RegisterMapper(RecordMapper.class)
    @SqlQuery("SELECT entry_number, entry.sha256hex as sha256hex, timestamp, content FROM item, entry WHERE item.sha256hex=entry.sha256hex and entry_number > :lastReadEntryNumber ORDER BY entry_number LIMIT 5000")
    List<Record> fetchRecordsAfter(@Bind("lastReadEntryNumber") int watermark);
}
