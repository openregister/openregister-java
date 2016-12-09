package uk.gov.register.core;

import uk.gov.register.service.ItemValidator;
import uk.gov.register.store.BackingStoreDriver;
import uk.gov.register.util.HashValue;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

public class ItemStore {
    private final BackingStoreDriver backingStoreDriver;
    private final ItemValidator itemValidator;

    public ItemStore(BackingStoreDriver backingStoreDriver, ItemValidator itemValidator) {
        this.backingStoreDriver = backingStoreDriver;
        this.itemValidator = itemValidator;
    }

    public void putItem(Item item) {
        itemValidator.validateItem(item.getContent());
        backingStoreDriver.insertItem(item);
    }

    public Optional<Item> getItemBySha256(HashValue hash) {
        return backingStoreDriver.getItemBySha256(hash);
    }

    public Collection<Item> getAllItems() {
        return backingStoreDriver.getAllItems();
    }

    public Iterator<Item> getIterator() {
        return backingStoreDriver.getItemIterator();
    }

    public Iterator<Item> getIterator(int start, int end){
        return backingStoreDriver.getItemIterator(start, end);
    }

}
