package uk.gov.register.db;

import org.skife.jdbi.v2.sqlobject.BindBean;
import uk.gov.register.util.EntryBlobPair;

public class InMemoryEntryBlobDAO implements EntryBlobDAO {
    @Override
    public void insertInBatch(@BindBean Iterable<EntryBlobPair> entries, String schema, String entryItemTable) {
        // Do nothing
    }
}
