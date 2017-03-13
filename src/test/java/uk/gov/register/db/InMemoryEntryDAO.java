package uk.gov.register.db;

import org.skife.jdbi.v2.ResultIterator;
import org.skife.jdbi.v2.sqlobject.Bind;
import uk.gov.register.core.Entry;
import uk.gov.register.store.postgres.BindEntry;

import java.time.Instant;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class InMemoryEntryDAO implements EntryQueryDAO, EntryDAO {
    private final List<Entry> entries;
    private int currentEntryNumber = 0;

    public InMemoryEntryDAO(List<Entry> entries) {
        this.entries = entries;
    }

    @Override
    public Optional<Entry> findByEntryNumber(int entryNumber) {
        if (entryNumber > entries.size()) {
            return Optional.empty();
        }
        return Optional.of(entries.get(entryNumber - 1));
    }

    @Override
    public Optional<Instant> getLastUpdatedTime() {
        if (entries.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(entries.get(entries.size()-1).getTimestamp());
    }

    @Override
    public int getTotalEntries() {
        return currentEntryNumber;
    }

    @Override
    public Collection<Entry> getAllEntriesNoPagination() {
        return entries;
    }

    @Override
    public Collection<Entry> getEntries(int start, int limit) {
        return entries.subList(start-1, start-1+limit);
    }

    @Override
    public ResultIterator<Entry> entriesIteratorFrom(int entryNumber) {
        return new FakeResultIterator(entries.subList(entryNumber-1, entries.size()).iterator());
    }

    @Override
    public Iterator<Entry> getIterator() {
        return entries.iterator();
    }

    @Override
    public Iterator<Entry> getIterator(int totalEntries1, int totalEntries2) {
        return entries.subList(totalEntries1, totalEntries2).iterator();
    }

    @Override
    public void insertInBatch(@BindEntry Iterable<Entry> entries) {
        for (Entry entry : entries) {
            if (!this.entries.contains(entry)) {
                this.entries.add(entry);
            }
        }
    }

    @Override
    public int currentEntryNumber() {
        return currentEntryNumber;
    }

    @Override
    public void setEntryNumber(@Bind("entryNumber") int currentEntryNumber) {
        this.currentEntryNumber = currentEntryNumber;
    }

    private class FakeResultIterator implements ResultIterator<Entry> {
        private final Iterator<Entry> iterator;

        public FakeResultIterator(Iterator<Entry> iterator) {
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
        public Entry next() {
            return iterator.next();
        }
    }
}
