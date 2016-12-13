package uk.gov.register.db;

import uk.gov.register.core.Item;
import uk.gov.register.core.ItemStore;
import uk.gov.register.util.HashValue;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

public abstract class AbstractItemStore implements ItemStore {
    protected final ItemQueryDAO itemQueryDAO;

    public AbstractItemStore(ItemQueryDAO itemQueryDAO) {
        this.itemQueryDAO = itemQueryDAO;
    }

    @Override
    public Optional<Item> getItemBySha256(HashValue hash) {
        checkpoint();
        return itemQueryDAO.getItemBySHA256(hash.getValue());
    }

    @Override
    public Collection<Item> getAllItems() {
        checkpoint();
        return itemQueryDAO.getAllItemsNoPagination();
    }

    @Override
    public Iterator<Item> getIterator() {
        checkpoint();
        return itemQueryDAO.getIterator();
    }

    @Override
    public Iterator<Item> getIterator(int start, int end) {
        checkpoint();
        return itemQueryDAO.getIterator(start, end);
    }

    public void checkpoint() {
        // by default, do nothing
    }
}
