package uk.gov.register.core;

import org.skife.jdbi.v2.Handle;
import uk.gov.register.db.ItemDAO;
import uk.gov.register.store.BackingStoreDriver;

import java.util.Collection;
import java.util.Optional;

public class ItemStore {
    private final BackingStoreDriver backingStoreDriver;

    public ItemStore(BackingStoreDriver backingStoreDriver) {
        this.backingStoreDriver = backingStoreDriver;
    }

    public void putItems(Handle handle, Iterable<Item> items) {
        handle.attach(ItemDAO.class).insertInBatch(items);
    }

    public Optional<Item> getItemBySha256(String sha256hex) {
        return backingStoreDriver.getItemBySha256(sha256hex);
    }

    public Collection<Item> getAllItems() {
        return backingStoreDriver.getAllItems();
    }
}
