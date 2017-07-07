package uk.gov.register.db;

import org.skife.jdbi.v2.sqlobject.Bind;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;
import uk.gov.register.store.postgres.BindItem;
import uk.gov.register.util.HashValue;

import java.util.*;
import java.util.stream.Collectors;

import static uk.gov.register.core.HashingAlgorithm.SHA256;

public class InMemoryItemDAO implements ItemDAO, ItemQueryDAO {
    private final Map<HashValue, Item> items;
    private EntryQueryDAO entryQueryDao;

    public InMemoryItemDAO(Map<HashValue, Item> items, EntryQueryDAO entryQueryDao) {
        this.items = items;
        this.entryQueryDao = entryQueryDao;
    }

    @Override
    public void insertInBatch(@BindItem Iterable<Item> items, String schema) {
        for (Item item : items) {
            this.items.put(item.getSha256hex(), item);
        }
    }

    @Override
    public Optional<Item> getItemBySHA256(@Bind("sha256hex") String sha256Hash, String schema) {
        return Optional.ofNullable(items.get(new HashValue(SHA256, sha256Hash)));
    }

    @Override
    public Collection<Item> getAllItemsNoPagination(String schema) {
        return items.values();
    }

    @Override
    public Iterator<Item> getIterator(String schema) {
        return getItemIteratorFromEntryIterator(entryQueryDao.getIterator(schema));
    }

    @Override
    public Iterator<Item> getIterator(@Bind("startEntryNo") int startEntryNo, @Bind("endEntryNo") int endEntryNo, String schema) {
        return getItemIteratorFromEntryIterator(entryQueryDao.getIterator(startEntryNo, endEntryNo, schema));
    }

    private Iterator<Item> getItemIteratorFromEntryIterator(Iterator<Entry> entryIterator) {
        List<Item> itemsResult = new ArrayList<>();
        entryIterator.forEachRemaining(entry -> {
            List<HashValue> hashValues = items.keySet().stream().filter(hashValue -> entry.getItemHashes().contains(hashValue)).collect(Collectors.toList());
            hashValues.forEach(hashValue -> itemsResult.add(items.remove(hashValue)));
        });
        return itemsResult.iterator();
    }
}
