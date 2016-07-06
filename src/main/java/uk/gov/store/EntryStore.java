package uk.gov.store;

import com.fasterxml.jackson.databind.JsonNode;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.TransactionIsolationLevel;
import org.skife.jdbi.v2.sqlobject.Transaction;
import org.skife.jdbi.v2.sqlobject.mixins.GetHandle;
import uk.gov.indexer.dao.DestinationDBUpdateDAO;
import uk.gov.mint.Entry;
import uk.gov.mint.Item;
import uk.gov.register.FatEntry;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public abstract class EntryStore implements GetHandle {
    private final EntryDAO entryDAO;
    private final ItemDAO itemDAO;
    private final DestinationDBUpdateDAO destinationDBUpdateDAO;

    public EntryStore() {
        Handle handle = getHandle();
        this.entryDAO = handle.attach(EntryDAO.class);
        this.itemDAO = handle.attach(ItemDAO.class);
        this.destinationDBUpdateDAO = handle.attach(DestinationDBUpdateDAO.class);
        this.entryDAO.ensureSchema();
        this.itemDAO.ensureSchema();
    }

    @Transaction(TransactionIsolationLevel.SERIALIZABLE)
    public void load(String registerName, Iterable<JsonNode> itemNodes) {
        AtomicInteger currentEntryNumber = new AtomicInteger(entryDAO.currentEntryNumber());
        List<Item> items = StreamSupport.stream(itemNodes.spliterator(), false)
                .map(Item::new)
                .collect(Collectors.toList());
        List<FatEntry> fatEntries = items.stream()
                .map(item -> new FatEntry(new Entry(currentEntryNumber.incrementAndGet(), item.getSha256hex()), item))
                .collect(Collectors.toList());
        List<Entry> entries = fatEntries.stream()
                .map(fatEntry -> fatEntry.entry)
                .collect(Collectors.toList());

        entryDAO.insertInBatch(entries);
        itemDAO.insertInBatch(items);
        destinationDBUpdateDAO.upsertInCurrentKeysTable(registerName, fatEntries);

        entryDAO.setEntryNumber(currentEntryNumber.get());
    }
}

