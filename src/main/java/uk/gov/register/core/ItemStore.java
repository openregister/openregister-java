package uk.gov.register.core;

import org.skife.jdbi.v2.Handle;
import uk.gov.register.db.ItemDAO;

public class ItemStore {
    public void putItems(Handle handle, Iterable<Item> items) {
        handle.attach(ItemDAO.class).insertInBatch(items);
    }
}
