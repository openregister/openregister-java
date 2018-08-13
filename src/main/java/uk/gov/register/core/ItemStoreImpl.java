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
    public Optional<Item> getItem(HashValue hash) {
        return dataAccessLayer.getItem(hash);
    }
    
    @Override
    public Collection<Item> getAllItems() {
        return dataAccessLayer.getAllItems();
    }

    @Override
    public Iterator<Item> getUserItemIterator() {
        return dataAccessLayer.getItemIterator(EntryType.user);
    }

    @Override
    public Iterator<Item> getUserItemIterator(int startEntryNumber, int endEntryNumber) {
        return dataAccessLayer.getItemIterator(startEntryNumber, endEntryNumber);
    }

    @Override
    public Iterator<Item> getSystemItemIterator() {
        return dataAccessLayer.getItemIterator(EntryType.system);
    }
}
