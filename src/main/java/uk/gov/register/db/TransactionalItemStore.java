package uk.gov.register.db;

import uk.gov.register.core.Item;
import uk.gov.register.core.ItemStore;
import uk.gov.register.service.ItemValidator;
import uk.gov.register.util.HashValue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

public class TransactionalItemStore implements ItemStore {
    private final ItemDAO itemDAO;
    private final ItemQueryDAO itemQueryDAO;
    private final ItemValidator itemValidator;
    private final Map<HashValue, Item> stagedItems;

    public TransactionalItemStore(ItemDAO itemDAO, ItemQueryDAO itemQueryDAO, ItemValidator itemValidator) {
        this.itemDAO = itemDAO;
        this.itemQueryDAO = itemQueryDAO;
        this.itemValidator = itemValidator;
        this.stagedItems = new HashMap<>();
    }

    @Override
    public void putItem(Item item) {
        itemValidator.validateItem(item.getContent());
        stagedItems.put(item.getSha256hex(), item);
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

    @Override
    public void checkpoint() {
        if (stagedItems.isEmpty()) {
            return;
        }
        itemDAO.insertInBatch(stagedItems.values());
        stagedItems.clear();
    }
}
