package uk.gov.register.db;

import uk.gov.register.core.Item;
import uk.gov.register.core.ItemStore;
import uk.gov.register.service.ItemValidator;
import uk.gov.register.util.HashValue;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

import static java.util.Collections.singletonList;

public class OnDemandItemStore implements ItemStore {
    private final ItemValidator itemValidator;
    private final ItemDAO itemDao;
    private final ItemQueryDAO itemQueryDao;

    @Inject
    public OnDemandItemStore(ItemValidator itemValidator, ItemDAO itemDao, ItemQueryDAO itemQueryDao) {
        this.itemValidator = itemValidator;
        this.itemDao = itemDao;
        this.itemQueryDao = itemQueryDao;
    }

    @Override public void putItem(Item item) {
        itemValidator.validateItem(item.getContent());
        itemDao.insertInBatch(singletonList(item));
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
