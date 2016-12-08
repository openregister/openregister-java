package uk.gov.register.db;

import uk.gov.register.core.Entry;

import java.util.List;

public class InMemoryEntryDAO implements EntryDAO {
    private final List<Entry> entries;

    public InMemoryEntryDAO(List<Entry> entries) {
        this.entries = entries;
    }

    @Override
    public void insertInBatch(Iterable<Entry> entries) {
        for (Entry entry : entries) {
            this.entries.add(entry);
        }
    }

    @Override
    public int currentEntryNumber() {
        return entries.size();
    }

    @Override
    public void setEntryNumber(int currentEntryNumber) {
        // ignored. probably shouldn't be
    }
}
