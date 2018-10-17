package uk.gov.register.db;

import org.skife.jdbi.v2.ResultIterator;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.customizers.Define;
import uk.gov.register.core.BaseEntry;
import uk.gov.register.store.postgres.BindEntry;

import java.time.Instant;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class InMemoryEntryDAO implements EntryDAO, EntryQueryDAO {
    private final List<BaseEntry> entries;
    private int currentEntryNumber = 0;

    public InMemoryEntryDAO(List<BaseEntry> entries) {
        this.entries = entries;
    }

    @Override
    public Optional<BaseEntry> findByEntryNumber(int entryNumber, String schema) {
        if (entryNumber > entries.size()) {
            return Optional.empty();
        }
        return Optional.of(entries.get(entryNumber - 1));
    }

    @Override
    public Optional<Instant> getLastUpdatedTime(String schema) {
        if (entries.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(entries.get(entries.size()-1).getTimestamp());
    }

    @Override
    public int getTotalEntries(String schema) {
        return currentEntryNumber;
    }

    @Override
    public int getTotalSystemEntries(String schema) {
        return currentEntryNumber;
    }

    @Override
    public Collection<BaseEntry> getAllEntriesNoPagination(String schema) {
        return entries;
    }

    @Override
    public Collection<BaseEntry> getEntries(int start, int limit, String schema) {
        return entries.subList(start-1, start-1+limit);
    }

    @Override
    public Collection<BaseEntry> getAllEntriesByKey(@Bind("key") String key, @Define("schema") String schema) {
        return entries.stream().filter(e -> e.getKey().equals(key)).collect(Collectors.toList());
    }

    @Override
    public Collection<BaseEntry> getEntriesByKeys(List<String> entryKeys, String schema, String entryTable, String entryItemTable) {
        return entries.stream().filter(e -> entryKeys.contains(e.getKey())).collect(Collectors.toList());
    }

    @Override
    public ResultIterator<BaseEntry> entriesIteratorFrom(int entryNumber, String schema) {
        return new FakeResultIterator(entries.subList(entryNumber-1, entries.size()).iterator());
    }

    @Override
    public Iterator<BaseEntry> getIterator(String schema) {
        return entries.iterator();
    }

    @Override
    public Iterator<BaseEntry> getIterator(int totalEntries1, int totalEntries2, String schema) {
        return entries.subList(totalEntries1, totalEntries2).iterator();
    }

    @Override
    public void insertInBatch(@BindEntry Iterable<BaseEntry> entries, String schema, String entryTable) {
        for (BaseEntry entry : entries) {
            if (!this.entries.contains(entry)) {
                this.entries.add(entry);
            }
        }
    }

    @Override
    public int currentEntryNumber(String schema) {
        return currentEntryNumber;
    }

    @Override
    public void setEntryNumber(@Bind("entryNumber") int currentEntryNumber, String schema) {
        this.currentEntryNumber = currentEntryNumber;
    }

    private class FakeResultIterator implements ResultIterator<BaseEntry> {
        private final Iterator<BaseEntry> iterator;

        public FakeResultIterator(Iterator<BaseEntry> iterator) {
            this.iterator = iterator;
        }

        @Override
        public void close() {
            // ignored
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public BaseEntry next() {
            return iterator.next();
        }
    }
}
