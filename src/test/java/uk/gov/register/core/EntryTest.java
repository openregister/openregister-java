package uk.gov.register.core;

import org.junit.Test;
import uk.gov.register.util.HashValue;

import java.time.Instant;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class EntryTest {
    private final HashValue hash1 = new HashValue(HashingAlgorithm.SHA256, "hash1");
    private final HashValue hash2 = new HashValue(HashingAlgorithm.SHA256, "hash2");
    private final HashValue blobHash1 = new HashValue(HashingAlgorithm.SHA256, "blobHash1");
    private final HashValue blobHash2 = new HashValue(HashingAlgorithm.SHA256, "blobHash2");

    @Test
    public void getTimestampAsISOFormat_returnsTheFormattedTimestamp() {
        Entry entry = new Entry(123, new HashValue(HashingAlgorithm.SHA256, "abc"), new HashValue(HashingAlgorithm.SHA256, "abc-blob-hash"), Instant.ofEpochSecond(1470403440), "sample-key", EntryType.user);
        assertThat(entry.getTimestampAsISOFormat(), equalTo("2016-08-05T13:24:00Z"));
    }

    @Test
    public void getSha256hex_returnsTheSha256HexOfItem() {
        Entry entry = new Entry(123, new HashValue(HashingAlgorithm.SHA256, "abc"), new HashValue(HashingAlgorithm.SHA256, "abc-blob-hash"), Instant.ofEpochSecond(1470403440), "sample-key", EntryType.user);
        assertThat(entry.getV1ItemHash().getValue(), equalTo("abc"));
    }

    @Test
    public void getItemHash_returnsSha256AsItemHash() {
        Entry entry = new Entry(123, new HashValue(HashingAlgorithm.SHA256, "abc"), new HashValue(HashingAlgorithm.SHA256, "abc-blob-hash"), Instant.ofEpochSecond(1470403440), "sample-key", EntryType.user);
        assertThat(entry.getV1ItemHash().toString(), equalTo("sha-256:abc"));
    }

    @Test
    public void getkey_returnskeyValue() {
        Entry entry = new Entry(123, new HashValue(HashingAlgorithm.SHA256, "abc"), new HashValue(HashingAlgorithm.SHA256, "abc-blob-hash"), Instant.ofEpochSecond(1470403440), "sample-key", EntryType.user);
        assertThat(entry.getKey(), equalTo("sample-key"));
    }

    @Test
    public void equals_shouldReturnTrue_whenEntriesAreEqual() {
        Entry entry1 = new Entry(1, hash1, blobHash1, Instant.parse("2017-03-10T00:00:00Z"), "key", EntryType.user);
        Entry entry2 = new Entry(1, hash1, blobHash1, Instant.parse("2017-03-10T00:00:00Z"), "key", EntryType.user);

        assertThat(entry1, equalTo(entry2));
    }

    @Test
    public void equals_shouldReturnFalse_whenKeyIsNotEqual() {
        Entry entry1 = new Entry(1, hash1, blobHash1, Instant.parse("2017-03-10T00:00:00Z"), "GB", EntryType.user);
        Entry entry2 = new Entry(1, hash1, blobHash1, Instant.parse("2017-03-10T00:00:00Z"), "US", EntryType.user);

        assertThat(entry1, not(entry2));
    }

    @Test
    public void equals_shouldReturnFalse_whenTimestampIsNotEqual() {
        Entry entry1 = new Entry(1, hash1, blobHash1, Instant.parse("2017-03-10T00:00:00Z"), "key", EntryType.user);
        Entry entry2 = new Entry(1, hash1, blobHash1, Instant.parse("2020-10-10T00:00:00Z"), "key", EntryType.user);

        assertThat(entry1, not(entry2));
    }

    @Test
    public void equals_shouldReturnFalse_whenItemHashIsNotEqual() {
        Entry entry1 = new Entry(1, hash1, blobHash1, Instant.parse("2017-03-10T00:00:00Z"), "key", EntryType.user);
        Entry entry2 = new Entry(1, hash2, blobHash2, Instant.parse("2017-03-10T00:00:00Z"), "key", EntryType.user);

        assertThat(entry1, not(entry2));
    }

    @Test
    public void equals_shouldReturnFalse_whenTypeIsNotEqual() {
        Entry entry1 = new Entry(1, hash1, blobHash1, Instant.parse("2017-03-10T00:00:00Z"), "key", EntryType.user);
        Entry entry2 = new Entry(1, hash1, blobHash1, Instant.parse("2017-03-10T00:00:00Z"), "key", EntryType.system);

        assertThat(entry1, not(entry2));
    }

    @Test
    public void hashcode_shouldReturnSameHashcode_whenEntriesAreEqual() {
        Entry entry1 = new Entry(1, hash1, blobHash1, Instant.parse("2017-03-10T00:00:00Z"), "key", EntryType.user);
        Entry entry2 = new Entry(1, hash1, blobHash1, Instant.parse("2017-03-10T00:00:00Z"), "key", EntryType.user);

        assertThat(entry1.hashCode(), equalTo(entry2.hashCode()));
    }

    @Test
    public void hashcode_shouldReturnDifferentHashCode_whenKeyIsNotEqual() {
        Entry entry1 = new Entry(1, hash1, blobHash1, Instant.parse("2017-03-10T00:00:00Z"), "GB", EntryType.user);
        Entry entry2 = new Entry(1, hash1, blobHash1, Instant.parse("2017-03-10T00:00:00Z"), "US", EntryType.user);

        assertThat(entry1.hashCode(), not(entry2.hashCode()));
    }

    @Test
    public void hashcode_shouldReturnDifferentHashCode_whenTimestampIsNotEqual() {
        Entry entry1 = new Entry(1, hash1, blobHash1, Instant.parse("2017-03-10T00:00:00Z"), "key", EntryType.user);
        Entry entry2 = new Entry(1, hash1, blobHash1, Instant.parse("2020-10-10T00:00:00Z"), "key", EntryType.user);

        assertThat(entry1.hashCode(), not(entry2.hashCode()));
    }

    @Test
    public void hashcode_shouldReturnDifferentHashCode_whenItemHashIsNotEqual() {
        Entry entry1 = new Entry(1, hash1, blobHash1, Instant.parse("2017-03-10T00:00:00Z"), "key", EntryType.user);
        Entry entry2 = new Entry(1, hash2, blobHash2, Instant.parse("2017-03-10T00:00:00Z"), "key", EntryType.user);

        assertThat(entry1.hashCode(), not(entry2.hashCode()));
    }

    @Test
    public void hashcode_shouldReturnDifferentHashCode_whenTypeIsNotEqual() {
        Entry entry1 = new Entry(1, hash1, blobHash1, Instant.parse("2017-03-10T00:00:00Z"), "key", EntryType.user);
        Entry entry2 = new Entry(1, hash1, blobHash1, Instant.parse("2017-03-10T00:00:00Z"), "key", EntryType.system);

        assertThat(entry1.hashCode(), not(entry2.hashCode()));
    }
}
