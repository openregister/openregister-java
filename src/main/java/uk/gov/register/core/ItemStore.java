package uk.gov.register.core;

import uk.gov.register.store.BackingStoreDriver;

import java.util.Collection;
import java.util.Optional;

public class ItemStore {
    private final BackingStoreDriver backingStoreDriver;

    public ItemStore(BackingStoreDriver backingStoreDriver) {
        this.backingStoreDriver = backingStoreDriver;
    }

    public void putItem(Item item) {
        backingStoreDriver.insertItem(item);
    }

    public Optional<Item> getItemBySha256(String sha256hex) {
        return backingStoreDriver.getItemBySha256(sha256hex);
    }

    public Collection<Item> getAllItems() {
        return backingStoreDriver.getAllItems();
    }
}
