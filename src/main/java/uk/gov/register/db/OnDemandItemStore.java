package uk.gov.register.db;

import uk.gov.register.core.Item;
import uk.gov.register.core.ItemStore;
import uk.gov.register.util.HashValue;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

public class OnDemandItemStore implements ItemStore {
    private final ItemQueryDAO itemQueryDao;

    @Inject
    public OnDemandItemStore(ItemQueryDAO itemQueryDao) {
        this.itemQueryDao = itemQueryDao;
    }

    @Override public void putItem(Item item) {
        throw new UnsupportedOperationException();
    }

    @Override public Optional<Item> getItemBySha256(HashValue hash) {
        return itemQueryDao.getItemBySHA256(hash.getValue());
    }

    @Override public Collection<Item> getAllItems() {
        return itemQueryDao.getAllItemsNoPagination();
    }

    @Override public Iterator<Item> getIterator() {
        return itemQueryDao.getIterator();
    }

    @Override public Iterator<Item> getIterator(int start, int end){
        return itemQueryDao.getIterator(start, end);
    }
}
