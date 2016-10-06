package uk.gov.register.core;

import org.skife.jdbi.v2.Handle;
import uk.gov.register.db.ItemDAO;
import uk.gov.register.db.ItemQueryDAO;

import java.util.Collection;
import java.util.Optional;

public class ItemStore {
    public void putItems(Handle handle, Iterable<Item> items) {
        handle.attach(ItemDAO.class).insertInBatch(items);
    }

    public Optional<Item> getItemBySha256(Handle h, String sha256hex) {
        return h.attach(ItemQueryDAO.class).getItemBySHA256(sha256hex);
    }

    public Collection<Item> getAllItems(Handle h) {
        return h.attach(ItemQueryDAO.class).getAllItemsNoPagination();
    }
}
