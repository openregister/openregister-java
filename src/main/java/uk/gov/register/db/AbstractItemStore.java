package uk.gov.register.db;

import uk.gov.register.core.Item;
import uk.gov.register.core.ItemStore;
import uk.gov.register.store.DataAccessLayer;
import uk.gov.register.util.HashValue;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

public abstract class AbstractItemStore implements ItemStore {
    private final DataAccessLayer dataAccessLayer;

    public AbstractItemStore(DataAccessLayer dataAccessLayer) {
        this.dataAccessLayer = dataAccessLayer;
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
}
