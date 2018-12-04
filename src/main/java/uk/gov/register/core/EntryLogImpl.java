package uk.gov.register.core;

import org.apache.commons.codec.digest.DigestUtils;
import uk.gov.register.proofs.V1EntryMerkleLeafStore;
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
        Optional<Record> record = dataAccessLayer.getRecord(entry.getEntryType(), entry.getKey());

        if (record.isPresent() && record.get().getEntry().getV1ItemHash().equals(entry.getV1ItemHash())) {
            throw new IndexingException(entry, "Cannot contain identical items to previous entry");
        }

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
    public Iterator<Entry> getEntryIterator(EntryType entryType) {
        return dataAccessLayer.getEntryIterator(entryType);
    }

    @Override
    public Iterator<Entry> getEntryIterator(EntryType entryType, int totalEntries1, int totalEntries2) {
        return dataAccessLayer.getEntryIterator(entryType, totalEntries1, totalEntries2);
    }

    @Override
    public Collection<Entry> getAllEntries() {
        return dataAccessLayer.getAllEntries();
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
    public RegisterProof getV1RegisterProof() {
        String rootHash = withVerifiableLog(verifiableLog -> bytesToString(verifiableLog.getCurrentRootHash()));

        return new RegisterProof(new HashValue(HashingAlgorithm.SHA256, rootHash), getTotalEntries(EntryType.user));
    }

    @Override
    public RegisterProof getV1RegisterProof(int totalEntries) {
        String rootHash = withVerifiableLog(verifiableLog -> bytesToString(verifiableLog.getSpecificRootHash(totalEntries)));

        return new RegisterProof(new HashValue(HashingAlgorithm.SHA256, rootHash), totalEntries);
    }

    @Override
    public EntryProof getV1EntryProof(int entryNumber, int totalEntries) {
        List<HashValue> auditProof = withVerifiableLog(verifiableLog ->
                verifiableLog.auditProof(entryNumber - 1, totalEntries)
                        .stream()
                        .map(hashBytes -> new HashValue(HashingAlgorithm.SHA256, bytesToString(hashBytes)))
                        .collect(Collectors.toList()));

        return new EntryProof(Integer.toString(entryNumber), auditProof);
    }

    @Override
    public ConsistencyProof getV1ConsistencyProof(int totalEntries1, int totalEntries2) {
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
            VerifiableLog verifiableLog = new VerifiableLog(DigestUtils.getSha256Digest(), new V1EntryMerkleLeafStore(entryIterator), memoizationStore);
            return callback.apply(verifiableLog);
        });
    }
}
