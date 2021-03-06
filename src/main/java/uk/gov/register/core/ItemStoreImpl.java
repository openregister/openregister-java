package uk.gov.register.core;

import uk.gov.register.store.DataAccessLayer;
import uk.gov.register.util.HashValue;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

public class ItemStoreImpl implements ItemStore {
    private final DataAccessLayer dataAccessLayer;

    public ItemStoreImpl(DataAccessLayer dataAccessLayer) {
        this.dataAccessLayer = dataAccessLayer;
    }

    @Override
    public void addItem(Item item) {
        dataAccessLayer.addItem(item);
    }

    @Override
    public Optional<Item> getItemByV1Hash(HashValue hash) {
        return dataAccessLayer.getItemByV1Hash(hash);
    }

    @Override
    public Optional<Item> getItem(HashValue hash) {
        return dataAccessLayer.getItem(hash);
    }

    @Override
    public Collection<Item> getAllItems() {
        return dataAccessLayer.getAllItems();
    }

    @Override
    public Collection<Item> getAllItems(EntryType entryType) {
        return dataAccessLayer.getAllItems(entryType);
    }

    @Override
    public Collection<Item> getUserItemsPaginated(Optional<HashValue> start, int limit) {
        return dataAccessLayer.getUserItemsPaginated(start, limit);
    }

    @Override
    public Iterator<Item> getItemIterator(EntryType entryType) {
        return dataAccessLayer.getItemIterator(entryType);
    }

    @Override
    public Iterator<Item> getUserItemIterator(int startEntryNumber, int endEntryNumber) {
        return dataAccessLayer.getItemIterator(startEntryNumber, endEntryNumber);
    }

    @Override
    public int getTotalItems() {
        return dataAccessLayer.getTotalItems();
    }
}
