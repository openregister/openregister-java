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
    public void putItem(Item item) {
        dataAccessLayer.putItem(item);
    }

    @Override
    public Optional<Item> getItemBySha256(HashValue hash) {
        return dataAccessLayer.getItemBySha256(hash);
    }

    @Override
    public Collection<Item> getAllItems() {
        return dataAccessLayer.getAllItems();
    }

    @Override
    public Iterator<Item> getIterator() {
        return dataAccessLayer.getItemIterator();
    }

    @Override
    public Iterator<Item> getIterator(int start, int end) {
        return dataAccessLayer.getItemIterator(start, end);
    }

    @Override
    public Iterator<Item> getSystemItemIterator() {
        return dataAccessLayer.getSystemItemIterator();
    }
}
