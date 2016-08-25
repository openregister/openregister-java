package uk.gov.register.db;

import com.fasterxml.jackson.databind.JsonNode;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.TransactionIsolationLevel;
import org.skife.jdbi.v2.sqlobject.Transaction;
import org.skife.jdbi.v2.sqlobject.mixins.GetHandle;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;
import uk.gov.register.core.Record;

import java.time.Instant;
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
        List<Record> records = items.stream()
                .map(item -> new Record(new Entry(currentEntryNumber.incrementAndGet(), item.getSha256hex(), Instant.now()), item))
                .collect(Collectors.toList());
        List<Entry> entries = records.stream()
                .map(record -> record.entry)
                .collect(Collectors.toList());

        entryDAO.insertInBatch(entries);
        itemDAO.insertInBatch(items);
        destinationDBUpdateDAO.upsertInCurrentKeysTable(registerName, records);

        entryDAO.setEntryNumber(currentEntryNumber.get());
    }
}

