package uk.gov.register.db;

import uk.gov.objecthash.IntegerValue;
import uk.gov.objecthash.ListValue;
import uk.gov.objecthash.ObjectHashable;
import uk.gov.objecthash.RawValue;
import uk.gov.objecthash.StringValue;
import uk.gov.objecthash.TimestampValue;
import uk.gov.register.core.Entry;
import uk.gov.verifiablelog.store.MerkleLeafStore;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class EntryMerkleLeafStore implements MerkleLeafStore {
    private final EntryIterator entryIterator;

    public EntryMerkleLeafStore(EntryIterator entryIterator) {
        this.entryIterator = entryIterator;
    }
    @Override
    public byte[] getLeafValue(int i) {
        return bytesFromEntry(entryIterator.findByEntryNumber(i + 1));
    }

    private byte[] bytesFromEntry(Entry entry) {
        List<ObjectHashable> entryHashes = Arrays.asList(
                new IntegerValue(entry.getEntryNumber()),
                new StringValue(entry.getKey()),
                new TimestampValue(entry.getTimestamp()),
                new RawValue(entry.getBlobHash().getValue().getBytes(StandardCharsets.UTF_8))
        );
        return new ListValue(entryHashes).digest();
    }

    @Override
    public int totalLeaves() {
        return entryIterator.getTotalEntries();
    }
}
