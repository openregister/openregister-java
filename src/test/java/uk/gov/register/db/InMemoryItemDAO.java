package uk.gov.register.db;

import org.skife.jdbi.v2.sqlobject.Bind;
import uk.gov.register.core.Item;
import uk.gov.register.store.postgres.BindItem;
import uk.gov.register.util.HashValue;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import static com.google.common.collect.Iterators.transform;
import static uk.gov.register.core.HashingAlgorithm.SHA256;

public class InMemoryItemDAO implements ItemDAO, ItemQueryDAO {
    private final Map<HashValue, Item> items;
    private EntryQueryDAO entryQueryDao;

    public InMemoryItemDAO(Map<HashValue, Item> items, EntryQueryDAO entryQueryDao) {
        this.items = items;
        this.entryQueryDao = entryQueryDao;
    }

    @Override
    public void insertInBatch(@BindItem Iterable<Item> items) {
        for (Item item : items) {
            this.items.put(item.getSha256hex(), item);
        }
    }

    @Override
    public Optional<Item> getItemBySHA256(@Bind("sha256hex") String sha256Hash) {
        return Optional.ofNullable(items.get(new HashValue(SHA256, sha256Hash)));
    }

    @Override
    public Collection<Item> getAllItemsNoPagination() {
        return items.values();
    }

    @Override
    public Iterator<Item> getIterator() {
        return transform(entryQueryDao.getIterator(),
                entry -> items.get(entry.getSha256hex()));
    }

    @Override
    public Iterator<Item> getIterator(@Bind("startEntryNo") int startEntryNo, @Bind("endEntryNo") int endEntryNo) {
        return transform(entryQueryDao.getIterator(startEntryNo, endEntryNo),
                entry -> items.get(entry.getSha256hex()));
    }
}
