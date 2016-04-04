package uk.gov.indexer.dao;

import com.google.common.collect.Lists;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.TransactionIsolationLevel;
import org.skife.jdbi.v2.sqlobject.Transaction;
import org.skife.jdbi.v2.sqlobject.mixins.GetHandle;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class DestinationDBUpdateDAO_NewSchema implements GetHandle, DBConnectionDAO {
    private final EntryUpdateDAO entryUpdateDAO;
    private final ItemUpdateDAO itemUpdateDAO;

    public DestinationDBUpdateDAO_NewSchema() {
        Handle handle = getHandle();
        entryUpdateDAO = handle.attach(EntryUpdateDAO.class);
        entryUpdateDAO.ensureEntryTableInPlace();

        itemUpdateDAO = handle.attach(ItemUpdateDAO.class);
        itemUpdateDAO.ensureItemTableInPlace();
    }

    public int lastReadEntryNumber() {
        return entryUpdateDAO.lastReadEntryNumber();
    }

    @Transaction(TransactionIsolationLevel.SERIALIZABLE)
    public void writeEntriesAndItemsInBatch(List<Entry> entries, List<Item> items) {
        Set<Item> newItems = extractNewItems(items);

        entryUpdateDAO.writeBatch(entries);
        if (!newItems.isEmpty()) {
            itemUpdateDAO.writeBatch(newItems);
        }
    }

    private Set<Item> extractNewItems(List<Item> items) {
        List<String> existingItemHexValues = itemUpdateDAO.getExistingItemHexValues(Lists.transform(items, Item::getSha256hex));
        return items.stream().filter(i -> !existingItemHexValues.contains(i.getSha256hex())).collect(Collectors.toSet());
    }
}
