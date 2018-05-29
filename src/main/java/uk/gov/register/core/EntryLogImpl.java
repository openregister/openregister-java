package uk.gov.register.core;

import org.apache.commons.codec.digest.DigestUtils;
import uk.gov.register.db.EntryMerkleLeafStore;
import uk.gov.register.exceptions.IndexingException;
import uk.gov.register.store.DataAccessLayer;
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

public class EntryLogImpl implements EntryLog {
    private final DataAccessLayer dataAccessLayer;
    private final MemoizationStore memoizationStore;

    public EntryLogImpl(DataAccessLayer dataAccessLayer, MemoizationStore memoizationStore) {
        this.dataAccessLayer = dataAccessLayer;
        this.memoizationStore = memoizationStore;
    }

    @Override
    public void appendEntry(Entry entry) throws IndexingException {
        dataAccessLayer.appendEntry(entry);
    }

    @Override
    public Optional<Entry> getEntry(int entryNumber) {
        return dataAccessLayer.getEntry(entryNumber);
    }

    @Override
    public Collection<Entry> getEntries(int start, int limit) {
        return dataAccessLayer.getEntries(start, limit);
    }

    @Override
    public Iterator<Entry> getEntryIterator(String indexName) {
        return dataAccessLayer.getEntryIterator(indexName);
    }

    @Override
    public Iterator<Entry> getEntryIterator(String indexName, int totalEntries1, int totalEntries2) {
        return dataAccessLayer.getEntryIterator(indexName, totalEntries1, totalEntries2);
    }

    @Override
    public Collection<Entry> getAllEntries() {
        return dataAccessLayer.getAllEntries();
    }

    @Override
    public int getTotalEntries() {
        return dataAccessLayer.getTotalEntries();
    }

    @Override
    public int getTotalEntries(EntryType entryType) {
        return dataAccessLayer.getTotalEntries(entryType);
    }

    @Override
    public Optional<Instant> getLastUpdatedTime() {
        return dataAccessLayer.getLastUpdatedTime();
    }

    @Override
    public RegisterProof getRegisterProof() {
        String rootHash = withVerifiableLog(verifiableLog -> bytesToString(verifiableLog.getCurrentRootHash()));

        return new RegisterProof(new HashValue(HashingAlgorithm.SHA256, rootHash), getTotalEntries());
    }

    @Override
    public RegisterProof getRegisterProof(int totalEntries) {
        String rootHash = withVerifiableLog(verifiableLog -> bytesToString(verifiableLog.getSpecificRootHash(totalEntries)));

        return new RegisterProof(new HashValue(HashingAlgorithm.SHA256, rootHash), totalEntries);
    }

    @Override
    public EntryProof getEntryProof(int entryNumber, int totalEntries) {
        List<HashValue> auditProof = withVerifiableLog(verifiableLog ->
                verifiableLog.auditProof(entryNumber - 1, totalEntries)
                        .stream()
                        .map(hashBytes -> new HashValue(HashingAlgorithm.SHA256, bytesToString(hashBytes)))
                        .collect(Collectors.toList()));

        return new EntryProof(Integer.toString(entryNumber), auditProof);
    }

    @Override
    public ConsistencyProof getConsistencyProof(int totalEntries1, int totalEntries2) {
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

    private <R> R withVerifiableLog(Function<VerifiableLog, R> callback) {
        return dataAccessLayer.withEntryIterator(entryIterator -> {
            VerifiableLog verifiableLog = new VerifiableLog(DigestUtils.getSha256Digest(), new EntryMerkleLeafStore(entryIterator), memoizationStore);
            return callback.apply(verifiableLog);
        });
    }
}
