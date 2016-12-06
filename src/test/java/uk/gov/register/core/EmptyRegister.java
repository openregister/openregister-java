package uk.gov.register.core;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.NotImplementedException;
import uk.gov.register.util.HashValue;
import uk.gov.register.views.ConsistencyProof;
import uk.gov.register.views.EntryProof;
import uk.gov.register.views.RegisterProof;
import uk.gov.verifiablelog.VerifiableLog;
import uk.gov.verifiablelog.store.MerkleLeafStore;

import javax.xml.bind.DatatypeConverter;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class EmptyRegister implements RegisterReadOnly {
    private final VerifiableLog verifiableLog;
    private final String registerName;
    private final RegisterMetadata registerMetadata;

    public EmptyRegister(RegisterMetadata registerMetadata) {
        this.registerName = registerMetadata.getRegisterName();
        this.registerMetadata = registerMetadata;
        verifiableLog = new VerifiableLog(DigestUtils.getSha256Digest(), new EmptyMerkleLeafStore());
    }

    public EmptyRegister(String registerName) {
        this(new RegisterMetadata(registerName, Collections.emptyList(), null, null, null, "alpha"));
    }

    public EmptyRegister() {
        this("widget");
    }

    @Override
    public String getRegisterName() {
        return registerName;
    }

    @Override
    public Optional<Item> getItemBySha256(HashValue hash) {
        return Optional.empty();
    }

    @Override
    public Collection<Item> getAllItems() {
        return Collections.emptyList();
    }

    @Override
    public Optional<Entry> getEntry(int entryNumber) {
        return Optional.empty();
    }

    @Override
    public Collection<Entry> getEntries(int start, int limit) {
        return Collections.emptyList();
    }

    @Override
    public Collection<Entry> getAllEntries() {
        return Collections.emptyList();
    }

    @Override
    public int getTotalEntries() {
        return 0;
    }

    @Override
    public Optional<Instant> getLastUpdatedTime() {
        return Optional.empty();
    }

    @Override
    public boolean containsField(String fieldName) {
        throw new NotImplementedException("test code anyway /shrug");
    }

    @Override
    public Optional<Record> getRecord(String key) {
        return Optional.empty();
    }

    @Override
    public int getTotalRecords() {
        return 0;
    }

    @Override
    public Collection<Entry> allEntriesOfRecord(String key) {
        return Collections.emptyList();
    }

    @Override
    public List<Record> getRecords(int limit, int offset) {
        return Collections.emptyList();
    }

    @Override
    public List<Record> max100RecordsFacetedByKeyValue(String key, String value) {
        return Collections.emptyList();
    }

    @Override
    public RegisterProof getRegisterProof() {
        String rootHash = bytesToString(verifiableLog.getCurrentRootHash());
        return new RegisterProof(new HashValue(HashingAlgorithm.SHA256, rootHash));
    }

    @Override
    public RegisterProof getRegisterProof(int entryNo) {
        throw new IllegalArgumentException();
    }

    @Override
    public EntryProof getEntryProof(int entryNumber, int totalEntries) {
        throw new IllegalArgumentException();
    }

    @Override
    public ConsistencyProof getConsistencyProof(int totalEntries1, int totalEntries2) {
        // The only valid call is between size 0 and size 0, which is the empty proof
        return new ConsistencyProof(Collections.emptyList());
    }

    @Override
    public Iterator<Entry> getEntryIterator() {
        return Collections.emptyIterator();
    }

    @Override
    public Iterator<Entry> getEntryIterator(int start, int end) {
        return Collections.emptyIterator();
    }

    @Override
    public Iterator<Item> getItemIterator() {
        return Collections.emptyIterator();
    }

    @Override
    public Iterator<Item> getItemIterator(int start, int end) {
        return Collections.emptyIterator();
    }

    @Override
    public RegisterMetadata getRegisterMetadata() {
        return registerMetadata;
    }

    private static class EmptyMerkleLeafStore implements MerkleLeafStore {
        @Override
        public byte[] getLeafValue(int i) {
            return new byte[0];
        }

        @Override
        public int totalLeaves() {
            return 0;
        }
    }

    private String bytesToString(byte[] bytes) {
        return DatatypeConverter.printHexBinary(bytes).toLowerCase();
    }
}
