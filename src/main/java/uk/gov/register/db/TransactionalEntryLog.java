package uk.gov.register.db;

import org.apache.commons.codec.digest.DigestUtils;
import uk.gov.register.core.Entry;
import uk.gov.register.core.EntryLog;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.util.HashValue;
import uk.gov.register.views.ConsistencyProof;
import uk.gov.register.views.EntryProof;
import uk.gov.register.views.RegisterProof;
import uk.gov.verifiablelog.VerifiableLog;
import uk.gov.verifiablelog.store.memoization.MemoizationStore;

import javax.xml.bind.DatatypeConverter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;

/**
 * An append-only log of Entries, together with proofs
 */
public class TransactionalEntryLog implements EntryLog {
    private final List<Entry> stagedEntries;
    private final EntryQueryDAO entryQueryDAO;
    private final EntryDAO entryDAO;
    private final VerifiableLog verifiableLog;

    public TransactionalEntryLog(MemoizationStore memoizationStore, EntryQueryDAO entryQueryDAO, EntryDAO entryDAO) {
        this.stagedEntries = new ArrayList<>();
        this.entryQueryDAO = entryQueryDAO;
        this.entryDAO = entryDAO;
        this.verifiableLog = new VerifiableLog(DigestUtils.getSha256Digest(), new EntryMerkleLeafStore(this.entryQueryDAO), memoizationStore);
    }

    @Override
    public void appendEntry(Entry entry) {
        stagedEntries.add(entry);
    }

    @Override
    public Optional<Entry> getEntry(int entryNumber) {
        checkpoint();
        return entryQueryDAO.findByEntryNumber(entryNumber);
    }

    @Override
    public Collection<Entry> getEntries(int start, int limit) {
        checkpoint();
        return entryQueryDAO.getEntries(start, limit);
    }

    @Override
    public Iterator<Entry> getIterator() {
        return entryQueryDAO.getIterator();
    }

    @Override
    public Iterator<Entry> getIterator(int start, int end) {
        return entryQueryDAO.getIterator(start, end);
    }

    @Override
    public Collection<Entry> getAllEntries() {
        checkpoint();
        return entryQueryDAO.getAllEntriesNoPagination();
    }

    @Override
    public int getTotalEntries() {
        OptionalInt maxStagedEntryNumber = getMaxStagedEntryNumber();
        return maxStagedEntryNumber.orElseGet(entryQueryDAO::getTotalEntries);
    }

    private OptionalInt getMaxStagedEntryNumber() {
        if (stagedEntries.isEmpty()) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(stagedEntries.get(stagedEntries.size() - 1).getEntryNumber());
    }

    @Override
    public Optional<Instant> getLastUpdatedTime() {
        // TODO: use an EntryMerkleLeafStore that can see the staged entries
        checkpoint();
        return entryQueryDAO.getLastUpdatedTime();
    }

    @Override
    public RegisterProof getRegisterProof() {
        checkpoint();
        String rootHash = bytesToString(verifiableLog.getCurrentRootHash());

        return new RegisterProof(new HashValue(HashingAlgorithm.SHA256, rootHash));
    }

    @Override
    public RegisterProof getRegisterProof(int totalEntries) {
        checkpoint();
        String rootHash = bytesToString(verifiableLog.getSpecificRootHash(totalEntries));

        return new RegisterProof(new HashValue(HashingAlgorithm.SHA256, rootHash));
    }

    @Override
    public EntryProof getEntryProof(int entryNumber, int totalEntries) {
        checkpoint();
        List<HashValue> auditProof =
                verifiableLog.auditProof(entryNumber, totalEntries)
                        .stream()
                        .map(hashBytes -> new HashValue(HashingAlgorithm.SHA256, bytesToString(hashBytes)))
                        .collect(Collectors.toList());

        return new EntryProof(Integer.toString(entryNumber), auditProof);
    }

    @Override
    public ConsistencyProof getConsistencyProof(int totalEntries1, int totalEntries2) {
        checkpoint();
        List<HashValue> consistencyProof =
                verifiableLog.consistencyProof(totalEntries1, totalEntries2)
                .stream()
                .map(hashBytes -> new HashValue(HashingAlgorithm.SHA256, bytesToString(hashBytes)))
                .collect(Collectors.toList());

        return new ConsistencyProof(consistencyProof);
    }

    private String bytesToString(byte[] bytes) {
        return DatatypeConverter.printHexBinary(bytes).toLowerCase();
    }

    @Override
    public void checkpoint() {
        if (stagedEntries.isEmpty()) {
            return;
        }
        entryDAO.insertInBatch(stagedEntries);
        entryDAO.setEntryNumber(entryDAO.currentEntryNumber() + stagedEntries.size());
        stagedEntries.clear();
    }
}
