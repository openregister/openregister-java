package uk.gov.register.db;

import org.skife.jdbi.v2.sqlobject.BindBean;
import uk.gov.register.util.EntryItemPair;

public class InMemoryEntryItemDAO implements EntryItemDAO {
    @Override
    public void insertInBatch(@BindBean Iterable<EntryItemPair> entries) {
        // Do nothing
    }
}
