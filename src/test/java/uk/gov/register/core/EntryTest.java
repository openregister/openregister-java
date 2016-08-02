package uk.gov.register.core;

import org.junit.Test;
import uk.gov.register.core.Entry;

import java.time.Instant;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class EntryTest {
    @Test
    public void getTimestamp_returnsTheFormattedTimestamp() {
        Entry entry = new Entry("123", "abc", Instant.ofEpochMilli(1459241964336L));
        assertThat(entry.getTimestamp(), equalTo("2016-03-29T08:59:24Z"));
    }

    @Test
    public void getSha256hex_returnsTheSha256HexOfItem() {
        Entry entry = new Entry("123", "abc", Instant.ofEpochMilli(1459241964336L));
        assertThat(entry.getSha256hex(), equalTo("sha-256:abc"));
    }
}
