package uk.gov.register.core;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.Test;
import uk.gov.register.util.HashValue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class EntryTest {
    private final HashValue hash1 = new HashValue(HashingAlgorithm.SHA256, "hash1");
    private final HashValue hash2 = new HashValue(HashingAlgorithm.SHA256, "hash2");

    @Test
    public void getTimestampAsISOFormat_returnsTheFormattedTimestamp() {
        Entry entry = new Entry(123, new HashValue(HashingAlgorithm.SHA256, "abc"), Instant.ofEpochSecond(1470403440), "sample-key", EntryType.user);
        assertThat(entry.getTimestampAsISOFormat(), equalTo("2016-08-05T13:24:00Z"));
    }

    @Test
    public void getSha256hex_returnsTheSha256HexOfItem() {
        Entry entry = new Entry(123, new HashValue(HashingAlgorithm.SHA256, "abc"), Instant.ofEpochSecond(1470403440), "sample-key", EntryType.user);
        assertThat(entry.getSha256hex().getValue(), equalTo("abc"));
    }

    @Test
    public void getItemHash_returnsSha256AsItemHash() {
        Entry entry = new Entry(123, new HashValue(HashingAlgorithm.SHA256, "abc"), Instant.ofEpochSecond(1470403440), "sample-key", EntryType.user);
        assertThat(entry.getSha256hex().toString(), equalTo("sha-256:abc"));
    }

    @Test
    public void getkey_returnskeyValue() {
        Entry entry = new Entry(123, new HashValue(HashingAlgorithm.SHA256, "abc"), Instant.ofEpochSecond(1470403440), "sample-key", EntryType.user);
        assertThat(entry.getKey(), equalTo("sample-key"));
    }

    @Test
    public void equals_shouldReturnTrue_whenEntriesAreEqual() {
        Entry entry1 = new Entry(1, hash1, Instant.parse("2017-03-10T00:00:00Z"), "key", EntryType.user);
        Entry entry2 = new Entry(1, hash1, Instant.parse("2017-03-10T00:00:00Z"), "key", EntryType.user);

        assertThat(entry1, equalTo(entry2));
    }

    @Test
    public void equals_shouldReturnFalse_whenKeyIsNotEqual() {
        Entry entry1 = new Entry(1, hash1, Instant.parse("2017-03-10T00:00:00Z"), "GB", EntryType.user);
        Entry entry2 = new Entry(1, hash1, Instant.parse("2017-03-10T00:00:00Z"), "US", EntryType.user);

        assertThat(entry1, not(entry2));
    }

    @Test
    public void equals_shouldReturnFalse_whenTimestampIsNotEqual() {
        Entry entry1 = new Entry(1, hash1, Instant.parse("2017-03-10T00:00:00Z"), "key", EntryType.user);
        Entry entry2 = new Entry(1, hash1, Instant.parse("2020-10-10T00:00:00Z"), "key", EntryType.user);

        assertThat(entry1, not(entry2));
    }

    @Test
    public void equals_shouldReturnTrue_whenListsAreNotInSameOrder() {
        List<HashValue> list1 = new ArrayList<>(Arrays.asList(hash1, hash2, hash1));
        List<HashValue> list2 = new ArrayList<>(Arrays.asList(hash2, hash1, hash1));

        assertThat(CollectionUtils.isEqualCollection(list1, list2), is(true));
    }

    @Test
    public void equals_shouldReturnTrue_whenItemHashesAreEqualButInDifferentOrder() {
        List<HashValue> list1 = new ArrayList<>(Arrays.asList(hash1, hash2));
        List<HashValue> list2 = new ArrayList<>(Arrays.asList(hash2, hash1));

        Entry entryWithList1 = new Entry(1, list1, Instant.parse("2017-03-10T00:00:00Z"), "key", EntryType.user);
        Entry entryWithList2 = new Entry(1, list2, Instant.parse("2017-03-10T00:00:00Z"), "key", EntryType.user);

        assertThat(entryWithList1.equals(entryWithList2), is(true));
    }

    @Test
    public void equals_shouldReturnTrue_whenCardinalityItemHashesIsEqual() {
        List<HashValue> list1 = new ArrayList<>(Arrays.asList(hash1, hash2, hash1));
        List<HashValue> list2 = new ArrayList<>(Arrays.asList(hash2, hash1, hash1));

        Entry entryWithList1 = new Entry(1, list1, Instant.parse("2017-03-10T00:00:00Z"), "key", EntryType.user);
        Entry entryWithList2 = new Entry(1, list2, Instant.parse("2017-03-10T00:00:00Z"), "key", EntryType.user);

        assertThat(entryWithList1.equals(entryWithList2), is(true));
    }

    @Test
    public void equals_shouldReturnFalse_whenCardinalityItemHashesIsNotEqual() {
        List<HashValue> list1 = new ArrayList<>(Arrays.asList(hash1, hash2, hash1));
        List<HashValue> list2 = new ArrayList<>(Arrays.asList(hash2, hash1, hash1, hash2));

        Entry entryWithList1 = new Entry(1, list1, Instant.parse("2017-03-10T00:00:00Z"), "key", EntryType.user);
        Entry entryWithList2 = new Entry(1, list2, Instant.parse("2017-03-10T00:00:00Z"), "key", EntryType.user);

        assertThat(entryWithList1.equals(entryWithList2), is(false));
    }

    @Test
    public void hashcode_shouldReturnSameHashcode_whenEntriesAreEqual() {
        Entry entry1 = new Entry(1, hash1, Instant.parse("2017-03-10T00:00:00Z"), "key", EntryType.user);
        Entry entry2 = new Entry(1, hash1, Instant.parse("2017-03-10T00:00:00Z"), "key", EntryType.user);

        assertThat(entry1.hashCode(), equalTo(entry2.hashCode()));
    }

    @Test
    public void hashcode_shouldReturnDifferentHashCode_whenKeyIsNotEqual() {
        Entry entry1 = new Entry(1, hash1, Instant.parse("2017-03-10T00:00:00Z"), "GB", EntryType.user);
        Entry entry2 = new Entry(1, hash1, Instant.parse("2017-03-10T00:00:00Z"), "US", EntryType.user);

        assertThat(entry1.hashCode(), not(entry2.hashCode()));
    }

    @Test
    public void hashcode_shouldReturnDifferentHashCode_whenTimestampIsNotEqual() {
        Entry entry1 = new Entry(1, hash1, Instant.parse("2017-03-10T00:00:00Z"), "key", EntryType.user);
        Entry entry2 = new Entry(1, hash1, Instant.parse("2020-10-10T00:00:00Z"), "key", EntryType.user);

        assertThat(entry1.hashCode(), not(entry2.hashCode()));
    }

    @Test
    public void hashcode_shouldReturnSameHashcode_whenItemHashesAreInEqualButInDifferentOrder() {
        List<HashValue> list1 = new ArrayList<>(Arrays.asList(hash1, hash2));
        List<HashValue> list2 = new ArrayList<>(Arrays.asList(hash2, hash1));

        Entry entryWithList1 = new Entry(1, list1, Instant.parse("2017-03-10T00:00:00Z"), "key", EntryType.user);
        Entry entryWithList2 = new Entry(1, list2, Instant.parse("2017-03-10T00:00:00Z"), "key", EntryType.user);

        assertThat(entryWithList1.hashCode(), equalTo(entryWithList2.hashCode()));
    }

    @Test
    public void hashcode_shouldReturnSameHashcode_whenCardinalityItemHashesIsEqual() {
        List<HashValue> list1 = new ArrayList<>(Arrays.asList(hash1, hash2, hash1));
        List<HashValue> list2 = new ArrayList<>(Arrays.asList(hash2, hash1, hash1));

        Entry entryWithList1 = new Entry(1, list1, Instant.parse("2017-03-10T00:00:00Z"), "key", EntryType.user);
        Entry entryWithList2 = new Entry(1, list2, Instant.parse("2017-03-10T00:00:00Z"), "key", EntryType.user);

        assertThat(entryWithList1.hashCode(), equalTo(entryWithList2.hashCode()));
    }

    @Test
    public void hashcode_shouldReturnDifferentHashCode_whenCardinalityItemHashesIsNotEqual() {
        List<HashValue> list1 = new ArrayList<>(Arrays.asList(hash1, hash2, hash1));
        List<HashValue> list2 = new ArrayList<>(Arrays.asList(hash2, hash1, hash1, hash2));

        Entry entryWithList1 = new Entry(1, list1, Instant.parse("2017-03-10T00:00:00Z"), "key", EntryType.user);
        Entry entryWithList2 = new Entry(1, list2, Instant.parse("2017-03-10T00:00:00Z"), "key", EntryType.user);

        assertThat(entryWithList1.hashCode(), not(entryWithList2.hashCode()));
    }
}
