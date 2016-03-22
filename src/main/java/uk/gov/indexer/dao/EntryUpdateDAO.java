package uk.gov.indexer.dao;

import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlBatch;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;

@UseStringTemplate3StatementLocator("/sql/init_entry.sql")
public interface EntryUpdateDAO extends DBConnectionDAO {

    String ENTRY_TABLE = "indexed_entry";

    @SqlUpdate
    void ensureEntryTableInPlace();

    @SqlQuery("SELECT MAX(entry_number) FROM " + ENTRY_TABLE)
    int lastReadEntryNumber();

    @SqlBatch("INSERT INTO " + ENTRY_TABLE + "(entry_number, sha256hex, timestamp) VALUES(:entryNumber, :itemHash, :timestamp)")
    void writeBatch(@BindBean Iterable<Entry> entries);
}
