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
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractEntryLog implements EntryLog {
    protected final EntryQueryDAO entryQueryDAO;
    private final MemoizationStore memoizationStore;

    protected AbstractEntryLog(EntryQueryDAO entryQueryDAO, MemoizationStore memoizationStore) {
        this.entryQueryDAO = entryQueryDAO;
        this.memoizationStore = memoizationStore;
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
        String rootHash = withVerifiableLog(verifiableLog -> bytesToString(verifiableLog.getCurrentRootHash()));

        return new RegisterProof(new HashValue(HashingAlgorithm.SHA256, rootHash), getTotalEntries());
    }

    @Override
    public RegisterProof getRegisterProof(int totalEntries) {
        checkpoint();
        String rootHash = withVerifiableLog(verifiableLog -> bytesToString(verifiableLog.getSpecificRootHash(totalEntries)));

        return new RegisterProof(new HashValue(HashingAlgorithm.SHA256, rootHash), totalEntries);
    }

    @Override
    public EntryProof getEntryProof(int entryNumber, int totalEntries) {
        checkpoint();
        List<HashValue> auditProof = withVerifiableLog(verifiableLog ->
                verifiableLog.auditProof(entryNumber - 1, totalEntries)
                        .stream()
                        .map(hashBytes -> new HashValue(HashingAlgorithm.SHA256, bytesToString(hashBytes)))
                        .collect(Collectors.toList()));

        return new EntryProof(Integer.toString(entryNumber), auditProof);
    }

    @Override
    public ConsistencyProof getConsistencyProof(int totalEntries1, int totalEntries2) {
        checkpoint();
        List<HashValue> consistencyProof = withVerifiableLog(verifiableLog ->
            verifiableLog.consistencyProof(totalEntries1, totalEntries2)
                    .stream()
                    .map(hashBytes -> new HashValue(HashingAlgorithm.SHA256, bytesToString(hashBytes)))
                    .collect(Collectors.toList()));

        return new ConsistencyProof(consistencyProof);
    }

    private String bytesToString(byte[] bytes) {
        return DatatypeConverter.printHexBinary(bytes).toLowerCase();
    }

    @Override
    public void checkpoint() {
        // by default, do nothing
    }

    private <R> R withVerifiableLog(Function<VerifiableLog, R> callback) {
        return EntryIterator.withEntryIterator(entryQueryDAO, entryIterator -> {
            VerifiableLog verifiableLog = new VerifiableLog(DigestUtils.getSha256Digest(), new EntryMerkleLeafStore(entryQueryDAO, entryIterator), memoizationStore);
            return callback.apply(verifiableLog);
        });
    }
}
