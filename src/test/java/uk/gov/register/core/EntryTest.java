package uk.gov.register.core;

import org.junit.Test;
import java.time.Instant;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class EntryTest {
    @Test
    public void getTimestampAsISOFormat_returnsTheFormattedTimestamp() {
        Entry entry = new Entry(123, "abc", Instant.ofEpochSecond(1470403440), "123");
        assertThat(entry.getTimestampAsISOFormat(), equalTo("2016-08-05T13:24:00Z"));
    }

    @Test
    public void getSha256hex_returnsTheSha256HexOfItem() {
        Entry entry = new Entry(123, "abc", Instant.ofEpochSecond(1470403440), "123");
        assertThat(entry.getSha256hex(), equalTo("abc"));
    }

    @Test
    public void getItemHash_returnsSha256AsItemHash() {
        Entry entry = new Entry(123, "abc", Instant.ofEpochSecond(1470403440), "123");
        assertThat(entry.getItemHash(), equalTo("sha-256:abc"));
    }

    @Test
    public void getItemKey_returnsItemKeyValue() {
        Entry entry = new Entry(123, "abc", Instant.ofEpochSecond(1470403440), "123");
        assertThat(entry.getItemHash(), equalTo("sha-256:abc"));
    }
}
