package uk.gov.indexer.dao;

import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlBatch;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

public interface EntryUpdateDAO extends DBConnectionDAO {

    String ENTRY_TABLE = "entry";

    @SqlUpdate("create table if not exists entry (entry_number SERIAL PRIMARY KEY, sha256hex VARCHAR, timestamp timestamp)")
    void ensureEntryTableInPlace();

    @SqlQuery("SELECT MAX(entry_number) FROM " + ENTRY_TABLE)
    int lastReadEntryNumber();

    @SqlBatch("INSERT INTO " + ENTRY_TABLE + "(entry_number, sha256hex, timestamp) VALUES(:entryNumber, :itemHash, :timestamp)")
    void writeBatch(@BindBean Iterable<Entry> entries);
}
