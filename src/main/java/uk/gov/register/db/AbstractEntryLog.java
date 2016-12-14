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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class AbstractEntryLog implements EntryLog {
    protected final EntryQueryDAO entryQueryDAO;
    protected final VerifiableLog verifiableLog;

    protected AbstractEntryLog(EntryQueryDAO entryQueryDAO, MemoizationStore memoizationStore) {
        this.entryQueryDAO = entryQueryDAO;
        verifiableLog = new VerifiableLog(DigestUtils.getSha256Digest(), new EntryMerkleLeafStore(this.entryQueryDAO), memoizationStore);
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
        checkpoint();
        return entryQueryDAO.getIterator();
    }

    @Override
    public Iterator<Entry> getIterator(int totalEntries1, int totalEntries2) {
        checkpoint();
        return entryQueryDAO.getIterator(totalEntries1, totalEntries2);
    }

    @Override
    public Collection<Entry> getAllEntries() {
        checkpoint();
        return entryQueryDAO.getAllEntriesNoPagination();
    }

    @Override
    public int getTotalEntries() {
        checkpoint();
        return entryQueryDAO.getTotalEntries();
    }

    @Override
    public Optional<Instant> getLastUpdatedTime() {
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
        // by default, do nothing
    }
}
