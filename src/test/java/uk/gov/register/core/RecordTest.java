package uk.gov.register.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.register.util.HashValue;

import java.io.IOException;
import java.time.Instant;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;

public class RecordTest {
    private final HashValue hash1 = new HashValue(HashingAlgorithm.SHA256, "hash1");
    private final HashValue hash2 = new HashValue(HashingAlgorithm.SHA256, "hash2");
    private final HashValue blobHash1 = new HashValue(HashingAlgorithm.SHA256, "blobHash1");
    private final HashValue blobHash2 = new HashValue(HashingAlgorithm.SHA256, "blobHash2");

    private final Item item1 = getItem("{\"key1\":\"value1\"}");
    private final Item item2 = getItem("{\"key2\":\"value2\"}");

    @Test
    public void recordsShouldBeEqualWithSameEntryAndItem() throws IOException {
        Record record1 = new Record(
                new Entry(1, hash1, blobHash1, Instant.parse("2017-03-10T00:00:00Z"), "key", EntryType.user), item1);
        Record record2 = new Record(
                new Entry(1, hash1, blobHash1, Instant.parse("2017-03-10T00:00:00Z"), "key", EntryType.user), item1);

        assertThat(record1.equals(record2), is(true));
    }

    @Test
    public void recordsShouldHaveSameHashCodeWithSameEntryAndItem() throws IOException {
        Record record1 = new Record(
                new Entry(1, hash1, blobHash1, Instant.parse("2017-03-10T00:00:00Z"), "key", EntryType.user), item1);
        Record record2 = new Record(
                new Entry(1, hash1, blobHash1, Instant.parse("2017-03-10T00:00:00Z"), "key", EntryType.user), item1);

        assertThat(record1.hashCode(), is(record2.hashCode()));
    }

    @Test
    public void recordsShouldNotBeEqualWithDifferentEntry() throws IOException {
        Record record1 = new Record(
                new Entry(1, hash1, blobHash1, Instant.parse("2017-03-10T00:00:00Z"), "key", EntryType.user), item1);
        Record record2 = new Record(
                new Entry(2, hash1, blobHash1, Instant.parse("2017-03-10T00:00:00Z"), "key", EntryType.user), item1);

        assertThat(record1.equals(record2), is(false));
    }

    @Test
    public void recordsShouldHaveDifferentHashCodesWithDifferentEntry() throws IOException {
        Record record1 = new Record(
                new Entry(1, hash1, blobHash1, Instant.parse("2017-03-10T00:00:00Z"), "key", EntryType.user), item1);
        Record record2 = new Record(
                new Entry(2, hash1, blobHash1, Instant.parse("2017-03-10T00:00:00Z"), "key", EntryType.user), item1);

        assertThat(record1.hashCode(), not(record2.hashCode()));
    }

    @Test
    public void recordsShouldNotBeEqualWithDifferentItems() throws IOException {
        Record record1 = new Record(
                new Entry(1, hash1, blobHash1, Instant.parse("2017-03-10T00:00:00Z"), "key", EntryType.user), item1);
        Record record2 = new Record(
                new Entry(1, hash2, blobHash2, Instant.parse("2017-03-10T00:00:00Z"), "key", EntryType.user), item2);

        assertThat(record1.equals(record2), is(false));
    }

    @Test
    public void recordsShouldHaveDifferentHashCodeWithDifferentItems() throws IOException {
        Record record1 = new Record(
                new Entry(1, hash1, blobHash1, Instant.parse("2017-03-10T00:00:00Z"), "key", EntryType.user), item1);
        Record record2 = new Record(
                new Entry(1, hash2, blobHash2, Instant.parse("2017-03-10T00:00:00Z"), "key", EntryType.user), item2);

        assertThat(record1.hashCode(), not(record2.hashCode()));
    }

    private Item getItem(String json) {
        try {
            return new Item(new ObjectMapper().readTree(json));
        } catch (IOException e) {
            return null;
        }
    }
}
