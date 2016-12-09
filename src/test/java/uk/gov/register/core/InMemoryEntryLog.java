package uk.gov.register.core;

import org.apache.commons.lang3.NotImplementedException;
import uk.gov.register.views.ConsistencyProof;
import uk.gov.register.views.EntryProof;
import uk.gov.register.views.RegisterProof;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class InMemoryEntryLog implements EntryLog {
    private List<Entry> entries = new ArrayList<>();

    @Override
    public void appendEntry(Entry entry) {
        entries.add(entry);
    }

    @Override
    public Optional<Entry> getEntry(int entryNumber) {
        return Optional.ofNullable(entries.get(entryNumber - 1));
    }

    @Override
    public Collection<Entry> getEntries(int start, int limit) {
        return entries.subList(start-1, start-1 + limit);
    }

    @Override
    public Iterator<Entry> getIterator() {
        return entries.iterator();
    }

    @Override
    public Iterator<Entry> getIterator(int totalEntries1, int totalEntries2) {
        return entries.subList(totalEntries1 -1, totalEntries2 -1).iterator();
    }

    @Override
    public Collection<Entry> getAllEntries() {
        return entries;
    }

    @Override
    public int getTotalEntries() {
        return entries.size();
    }

    @Override
    public Optional<Instant> getLastUpdatedTime() {
        if (entries.size() == 0) {
            return Optional.empty();
        }
        return Optional.of(entries.get(entries.size()-1).getTimestamp());
    }

    @Override
    public RegisterProof getRegisterProof(){
        throw new NotImplementedException("meh");
    }

    @Override
    public RegisterProof getRegisterProof(int totalEntries) {
        throw new NotImplementedException("meh");
    }

    @Override
    public EntryProof getEntryProof(int entryNumber, int totalEntries) {
        throw new NotImplementedException("meh");
    }

    @Override
    public ConsistencyProof getConsistencyProof(int totalEntries1, int totalEntries2) {
        throw new NotImplementedException("meh");
    }
}
