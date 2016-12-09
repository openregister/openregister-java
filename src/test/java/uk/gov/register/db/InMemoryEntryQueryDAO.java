package uk.gov.register.db;

import org.skife.jdbi.v2.ResultIterator;
import uk.gov.register.core.Entry;

import java.time.Instant;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class InMemoryEntryQueryDAO implements EntryQueryDAO {
    private final List<Entry> entries;

    public InMemoryEntryQueryDAO(List<Entry> entries) {
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
        return entries.size();
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
    public Iterator<Entry> getIterator(int startEntryNo, int endEntryNo) {
        return entries.subList(startEntryNo-1, endEntryNo-1).iterator();
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
