package uk.gov.register.db;

import org.skife.jdbi.v2.sqlobject.BindBean;
import uk.gov.register.util.EntryItemPair;

public class InMemoryEntryBlobDAO implements EntryBlobDAO {
    @Override
    public void insertInBatch(@BindBean Iterable<EntryItemPair> entries, String schema, String entryItemTable) {
        // Do nothing
    }
}
