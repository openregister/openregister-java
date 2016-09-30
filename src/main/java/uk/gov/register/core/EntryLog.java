package uk.gov.register.core;

import org.apache.commons.codec.digest.DigestUtils;
import org.skife.jdbi.v2.Handle;
import uk.gov.register.db.EntryDAO;
import uk.gov.register.db.EntryMerkleLeafStore;
import uk.gov.register.db.EntryQueryDAO;
import uk.gov.register.views.ConsistencyProof;
import uk.gov.register.views.EntryProof;
import uk.gov.register.views.RegisterProof;
import uk.gov.verifiablelog.VerifiableLog;
import uk.gov.verifiablelog.store.memoization.MemoizationStore;

import javax.inject.Inject;
import javax.xml.bind.DatatypeConverter;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * An append-only log of Entries, together with proofs
 */
public class EntryLog {
    private final MemoizationStore memoizationStore;

    @Inject
    public EntryLog(MemoizationStore memoizationStore) {
        this.memoizationStore = memoizationStore;
    }

    public void appendEntries(Handle handle, List<Entry> entries) {
        EntryDAO entryDAO = handle.attach(EntryDAO.class);
        entryDAO.insertInBatch(entries);
        entryDAO.setEntryNumber(entryDAO.currentEntryNumber() + entries.size());
    }

    public Optional<Entry> getEntry(Handle handle, int entryNumber) {
        return handle.attach(EntryQueryDAO.class).findByEntryNumber(entryNumber);
    }

    public Collection<Entry> getEntries(Handle h, int start, int limit) {
        return h.attach(EntryQueryDAO.class).getEntries(start, limit);
    }

    public Collection<Entry> getAllEntries(Handle handle) {
        return handle.attach(EntryQueryDAO.class).getAllEntriesNoPagination();
    }

    public int getTotalEntries(Handle h) {
        return h.attach(EntryQueryDAO.class).getTotalEntries();
    }

    public Optional<Instant> getLastUpdatedTime(Handle h) {
        return h.attach(EntryQueryDAO.class).getLastUpdatedTime();
    }

    public RegisterProof getRegisterProof(Handle handle) throws NoSuchAlgorithmException {
        VerifiableLog verifiableLog = getVerifiableLog(handle);
        String rootHash = bytesToString(verifiableLog.currentRoot());
        return new RegisterProof(rootHash);
    }

    public EntryProof getEntryProof(Handle handle, int entryNumber, int totalEntries) {
        VerifiableLog verifiableLog = getVerifiableLog(handle);

        List<String> auditProof = verifiableLog.auditProof(entryNumber, totalEntries).stream()
                .map(this::bytesToString).collect(Collectors.toList());

        return new EntryProof(Integer.toString(entryNumber), auditProof);
    }

    public ConsistencyProof getConsistencyProof(Handle handle, int totalEntries1, int totalEntries2){
        VerifiableLog verifiableLog = getVerifiableLog(handle);
        List<String> consistencyProof = verifiableLog.consistencyProof(totalEntries1, totalEntries2).stream()
                .map(this::bytesToString).collect(Collectors.toList());

        return new ConsistencyProof(consistencyProof);
    }

    private VerifiableLog getVerifiableLog(Handle handle) {
        EntryMerkleLeafStore merkleLeafStore = new EntryMerkleLeafStore(handle.attach(EntryQueryDAO.class));
        return new VerifiableLog(DigestUtils.getSha256Digest(), merkleLeafStore, memoizationStore);
    }

    private String bytesToString(byte[] bytes) {
        return DatatypeConverter.printHexBinary(bytes).toLowerCase();
    }
}
