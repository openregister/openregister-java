package uk.gov.register.core;

import uk.gov.register.service.ItemValidator;
import uk.gov.register.store.BackingStoreDriver;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

public class ItemStore {
    private final BackingStoreDriver backingStoreDriver;
    private final ItemValidator itemValidator;
    private final String registerName;

    public ItemStore(BackingStoreDriver backingStoreDriver, ItemValidator itemValidator, String registerName) {
        this.backingStoreDriver = backingStoreDriver;
        this.itemValidator = itemValidator;
        this.registerName = registerName;
    }

    public void putItem(Item item) {
        itemValidator.validateItem(registerName, item.getContent());
        backingStoreDriver.insertItem(item);
    }

    public Optional<Item> getItemBySha256(String sha256hex) {
        return backingStoreDriver.getItemBySha256(sha256hex);
    }

    public Collection<Item> getAllItems() {
        return backingStoreDriver.getAllItems();
    }

    public Iterator<Item> getIterator(int start, int end){
        return backingStoreDriver.getItemIterator(start, end);
    }
}
