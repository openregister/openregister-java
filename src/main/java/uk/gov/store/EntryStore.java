package uk.gov.store;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Iterables;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.TransactionIsolationLevel;
import org.skife.jdbi.v2.sqlobject.Transaction;
import org.skife.jdbi.v2.sqlobject.mixins.GetHandle;
import uk.gov.mint.Entry;
import uk.gov.mint.Item;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

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
    public void load(Iterable<JsonNode> itemNodes) {
        AtomicInteger currentEntryNumber = new AtomicInteger(entryDAO.currentEntryNumber());
        Iterable<Item> items = Iterables.transform(itemNodes, Item::new);
        Iterable<Entry> entries = Iterables.transform(items, item -> new Entry(currentEntryNumber.incrementAndGet(), item.getSha256hex()));

        entryDAO.insertInBatch(entries);
        itemDAO.insertInBatch(items);

        entryDAO.setEntryNumber(currentEntryNumber.get());
    }



}

