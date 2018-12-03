package uk.gov.register.db;

import org.junit.Test;
import uk.gov.register.core.Entry;
import uk.gov.register.core.EntryType;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.util.HashValue;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EntryMerkleLeafStoreTest {
    @Test
    public void testEntryMerkleLeafStoreComputesObjecthash() {
        Entry entry = new Entry(
                6,
                new HashValue(HashingAlgorithm.SHA256, "v1Hash"),
                new HashValue(HashingAlgorithm.SHA256, "6b18693874513ba13da54d61aafa7cad0c8f5573f3431d6f1c04b07ddb27d6bb"),
                OffsetDateTime.of(2016,4,5,13, 23, 5, 0, ZoneOffset.UTC).toInstant(),
                "GB",
                EntryType.user
        );
        EntryIterator entryIterator = mock(EntryIterator.class);
        when(entryIterator.findByEntryNumber(1)).thenReturn(entry);
        EntryMerkleLeafStore store = new EntryMerkleLeafStore(entryIterator);

        byte[] value = store.getLeafValue(0);
        String valueHex = bytesToHex(value);

        assertEquals("34e124de59e73cb802474faba5b5434983eaeef902d6ec24e3421ebb78bf2c69", valueHex);
    }

    private String bytesToHex(byte[] value) {
        StringBuilder sb = new StringBuilder();
        for (byte b : value) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
