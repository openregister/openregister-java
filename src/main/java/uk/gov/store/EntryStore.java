package uk.gov.store;

import com.google.common.collect.Lists;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.TransactionIsolationLevel;
import org.skife.jdbi.v2.sqlobject.Transaction;
import org.skife.jdbi.v2.sqlobject.mixins.GetHandle;
import uk.gov.mint.DataReplicationTask;
import uk.gov.mint.Item;

import java.util.List;
import java.util.stream.Collectors;

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
    public void load(List<Item> items){
        itemDAO.insertInBatch(items);
        entryDAO.insertInBatch(Lists.transform(items, Item::getSha256hex));
    }

    //TODO: methods below are specific to migration which must be deleted after migration is completed
    @Transaction(TransactionIsolationLevel.SERIALIZABLE)
    public void migrate(List<DataReplicationTask.MigratedEntry> migratedEntries) {
        List<Item> items = Lists.transform(migratedEntries, DataReplicationTask.MigratedEntry::getItem);

        List<String> existingItemHex = itemDAO.existingItemHex(Lists.transform(items, Item::getSha256hex));

        List<Item> newItems = items.stream().filter(i -> !existingItemHex.contains(i.getSha256hex())).collect(Collectors.toList());

        itemDAO.insertInBatch(newItems);
        entryDAO.insertMigratedEntries(migratedEntries);
        entryDAO.updateSequenceNumber(migratedEntries.get(migratedEntries.size() - 1).getId());
    }

    public int lastMigratedID() {
        return entryDAO.maxID();
    }
}

