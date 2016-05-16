package uk.gov.store;

import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;
import uk.gov.mint.Entry;

@UseStringTemplate3StatementLocator("/sql/entry.sql")
interface EntryDAO {
    @SqlUpdate
    void ensureSchema();

    @SqlBatch("insert into entry(entry_number, sha256hex) values(:entry_number, :sha256hex)")
    void insertInBatch(@BindBean Iterable<Entry> entries);

    @SqlQuery("select value from current_entry_number")
    int currentEntryNumber();

    @SqlUpdate("update current_entry_number set value=:entry_number")
    void setEntryNumber(@Bind("entry_number") int currentEntryNumber);
}
