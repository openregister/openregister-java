package uk.gov.register.db;

import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;
import uk.gov.register.core.Entry;

@UseStringTemplate3StatementLocator("/sql/entry.sql")
public interface EntryDAO {
    @SqlUpdate
    void ensureSchema();

    @SqlBatch("insert into entry(entry_number, sha256hex, timestamp) values(:entryNumber, :sha256hex, :timestampAsLong)")
    void insertInBatch(@BindBean Iterable<Entry> entries);

    @SqlQuery("select value from current_entry_number")
    int currentEntryNumber();

    @SqlUpdate("update current_entry_number set value=:entryNumber")
    void setEntryNumber(@Bind("entryNumber") int currentEntryNumber);
}
