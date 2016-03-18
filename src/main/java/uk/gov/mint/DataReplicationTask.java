package uk.gov.mint;

import com.google.common.collect.Lists;
import uk.gov.store.EntriesUpdateDAO;
import uk.gov.store.EntryStore;
import uk.gov.store.OldSchemaEntry;

import java.util.List;

public class DataReplicationTask implements Runnable {
    private EntriesUpdateDAO entriesUpdateDAO;
    private EntryStore entryStore;

    public DataReplicationTask(EntriesUpdateDAO entriesUpdateDAO, EntryStore entryStore) {
        this.entriesUpdateDAO = entriesUpdateDAO;
        this.entryStore = entryStore;
    }

    @Override
    public void run() {
        for (List<OldSchemaEntry> entries; !(entries = entriesUpdateDAO.read(lastReadID())).isEmpty(); ) {
            entryStore.migrate(Lists.transform(entries, MigratedEntry::new));
        }
    }

    public static class MigratedEntry {
        private final int id;
        private final Item item;

        public MigratedEntry(OldSchemaEntry oldSchemaEntry) {
            this.id = oldSchemaEntry.id;
            this.item = new Item(oldSchemaEntry.entry.get("entry"));
        }

        public Item getItem() {
            return item;
        }

        public String getSha256hex(){
            return item.getSha256hex();
        }

        public int getId() {
            return id;
        }
    }

    private int lastReadID() {
        return entryStore.lastMigratedID();
    }
}

