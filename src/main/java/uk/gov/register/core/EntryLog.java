package uk.gov.register.core;

import uk.gov.register.store.BackingStoreDriver;
import uk.gov.register.util.HashValue;
import uk.gov.register.views.ConsistencyProof;
import uk.gov.register.views.EntryProof;
import uk.gov.register.views.RegisterProof;

import javax.xml.bind.DatatypeConverter;
import java.time.Instant;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * An append-only log of Entries, together with proofs
 */
public class EntryLog {
    private final BackingStoreDriver backingStoreDriver;

    public EntryLog(BackingStoreDriver backingStoreDriver) {
        this.backingStoreDriver = backingStoreDriver;
    }

    public void appendEntry(Entry entry) {
        backingStoreDriver.insertEntry(entry);
    }

    public Optional<Entry> getEntry(int entryNumber) {
        return backingStoreDriver.getEntry(entryNumber);
    }

    public Collection<Entry> getEntries(int start, int limit) {
        return backingStoreDriver.getEntries(start, limit);
    }

    public Iterator<Entry> getIterator() {
        return backingStoreDriver.getEntryIterator();
    }

    public Iterator<Entry> getIterator(int start, int end){
        return backingStoreDriver.getEntryIterator(start, end);
    }

    public Collection<Entry> getAllEntries() {
        return backingStoreDriver.getAllEntries();
    }

    public int getTotalEntries() {
        return backingStoreDriver.getTotalEntries();
    }

    public Optional<Instant> getLastUpdatedTime() {
        return backingStoreDriver.getLastUpdatedTime();
    }

    public RegisterProof getRegisterProof() {
        String rootHash =  backingStoreDriver.withVerifiableLog(verifiableLog ->
                bytesToString(verifiableLog.getCurrentRootHash()));

        return new RegisterProof(new HashValue(HashingAlgorithm.SHA256, rootHash));
    }

    public RegisterProof getRegisterProof(int totalEntries) {
        String rootHash = backingStoreDriver.withVerifiableLog(verifiableLog ->
                bytesToString(verifiableLog.getSpecificRootHash(totalEntries)));

        return new RegisterProof(new HashValue(HashingAlgorithm.SHA256, rootHash));
    }

    public EntryProof getEntryProof(int entryNumber, int totalEntries) {
        List<HashValue> auditProof = backingStoreDriver.withVerifiableLog(verifiableLog ->
                verifiableLog.auditProof(entryNumber, totalEntries)
                        .stream()
                        .map(hashBytes -> new HashValue(HashingAlgorithm.SHA256, bytesToString(hashBytes)))
                        .collect(Collectors.toList()));

        return new EntryProof(Integer.toString(entryNumber), auditProof);
    }

    public ConsistencyProof getConsistencyProof(int totalEntries1, int totalEntries2) {
        List<HashValue> consistencyProof = backingStoreDriver.withVerifiableLog(verifiableLog ->
                verifiableLog.consistencyProof(totalEntries1, totalEntries2))
                .stream()
                .map(hashBytes -> new HashValue(HashingAlgorithm.SHA256, bytesToString(hashBytes)))
                .collect(Collectors.toList());

        return new ConsistencyProof(consistencyProof);
    }

    private String bytesToString(byte[] bytes) {
        return DatatypeConverter.printHexBinary(bytes).toLowerCase();
    }
}
