package uk.gov.register.db;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import uk.gov.register.core.Entry;
import uk.gov.register.util.HashValue;
import uk.gov.verifiablelog.store.memoization.DoNothing;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static uk.gov.register.core.HashingAlgorithm.SHA256;

public class TransactionalEntryLogTest {

    InMemoryEntryDAO entryQueryDAO;
    List<Entry> entries;
    private TransactionalEntryLog entryLog;
    private final Entry entry1 = new Entry(1, new HashValue(SHA256, "abc"), Instant.ofEpochMilli(123), "key1");
    private final Entry entry2 = new Entry(2, new HashValue(SHA256, "def"), Instant.ofEpochMilli(124), "key2");

    @Before
    public void setUp() throws Exception {
        entries = new ArrayList<>();
        entryQueryDAO = new InMemoryEntryDAO(entries);
        entryLog = new TransactionalEntryLog(new DoNothing(), entryQueryDAO, entryQueryDAO, mock(EntryItemDAO.class));
    }

    @Test
    public void appendEntry_shouldNotCommitData() {
        entryLog.appendEntry(new Entry(1, new HashValue(SHA256, "abc"), Instant.ofEpochMilli(123), "key1"));
        entryLog.appendEntry(new Entry(2, new HashValue(SHA256, "def"), Instant.ofEpochMilli(124), "key2"));

        assertThat(entries, is(empty()));
    }

    @Test
    public void getEntry_shouldGetFromStagedDataIfNeeded() throws Exception {
        Entry entry1 = new Entry(1, new HashValue(SHA256, "abc"), Instant.ofEpochMilli(123), "key1");
        entryLog.appendEntry(entry1);
        entryLog.appendEntry(new Entry(2, new HashValue(SHA256, "def"), Instant.ofEpochMilli(124), "key2"));

        assertThat(entryLog.getEntry(1), equalTo(Optional.of(entry1)));
    }

    @Test
    public void getEntries_shouldGetFromStagedDataIfNeeded() throws Exception {
        entryLog.appendEntry(entry1);
        entryLog.appendEntry(entry2);

        assertThat(entryLog.getEntries(1, 2), equalTo(ImmutableList.of(entry1, entry2)));
    }

    @Test
    public void getAllEntries_shouldGetFromStagedDataIfNeeded() throws Exception {
        entryLog.appendEntry(entry1);
        entryLog.appendEntry(entry2);

        assertThat(entryLog.getAllEntries(), equalTo(ImmutableList.of(entry1, entry2)));
    }

    @Test
    public void getTotalEntries_shouldCountStagedDataWithoutCommitting() throws Exception {
        // existing entry in backing store
        entries.add(entry1);
        // entry in staging area
        entryLog.appendEntry(entry2);

        int totalEntries = entryLog.getTotalEntries();

        assertThat(totalEntries, equalTo(2)); // we counted the staged entry
        assertThat(entries, equalTo(singletonList(entry1))); // but we didn't commit it to backing store
    }

    @Test
    public void getLastTimeUpdated_shouldGetFromStagedDataIfNeeded() throws Exception {
        entryLog.appendEntry(entry1);
        entryLog.appendEntry(entry2);

        assertThat(entryLog.getLastUpdatedTime(), equalTo(Optional.of(entry2.getTimestamp())));
    }
}
