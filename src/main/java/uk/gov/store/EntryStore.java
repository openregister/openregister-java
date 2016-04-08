package uk.gov.store;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.TransactionIsolationLevel;
import org.skife.jdbi.v2.sqlobject.Transaction;
import org.skife.jdbi.v2.sqlobject.mixins.GetHandle;
import uk.gov.mint.Item;

import java.util.Set;

public abstract class EntryStore implements GetHandle {
    private final EntryDAO entryDAO;
    private final ItemDAO itemDAO;

    public EntryStore() {
        Handle handle = getHandle();
        this.entryDAO = handle.attach(EntryDAO.class);
        this.itemDAO = handle.attach(ItemDAO.class);
        entryDAO.ensureSchema();
        itemDAO.ensureSchema();
    }

    @Transaction(TransactionIsolationLevel.SERIALIZABLE)
    public void load(Iterable<Item> items) {
        entryDAO.insertInBatch(Iterables.transform(items, Item::getSha256hex));
        itemDAO.insertInBatch(extractNewItems(items));
    }

    private Set<Item> extractNewItems(Iterable<Item> items) {
        Iterable<String> existingItemHex = itemDAO.existingItemHex(Lists.newArrayList(Iterables.transform(items, Item::getSha256hex)));
        return ImmutableSet.copyOf(
                Iterables.filter(
                        items,
                        item -> !Iterables.contains(existingItemHex, item.getSha256hex())
                )
        );
    }
}

