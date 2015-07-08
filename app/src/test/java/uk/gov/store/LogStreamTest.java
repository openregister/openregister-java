package uk.gov.store;

import com.google.common.collect.ImmutableList;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.OngoingStubbing;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LogStreamTest {
    @Mock
    private HighWaterMarkDAO highWaterMarkDAO;
    @Mock
    private EntriesQueryDAO entriesQueryDAO;
    @Mock
    private KafkaProducer<String, byte[]> kafkaProducer;

    @Test
    public void shouldSendNoEntriesWhenNothingToUpdate() throws Exception {
        LogStream logStream = new LogStream(highWaterMarkDAO, entriesQueryDAO, kafkaProducer);

        when(highWaterMarkDAO.getCurrentHighWaterMark()).thenReturn(0);
        whenEntriesAreInDatabase(0, Collections.emptyList());

        logStream.notifyOfNewEntries();

        verifyEntriesAreSent(Collections.emptyList());
        verifyNoMoreInteractions(kafkaProducer);
    }

    @Test
    public void shouldSendOneEntryWhenRequired() throws Exception {
        LogStream logStream = new LogStream(highWaterMarkDAO, entriesQueryDAO, kafkaProducer);

        ImmutableList<IndexedEntry> entries = ImmutableList.of(entry(1, "entry1"));
        when(highWaterMarkDAO.getCurrentHighWaterMark()).thenReturn(0);
        whenEntriesAreInDatabase(0, entries);

        logStream.notifyOfNewEntries();

        verifyEntriesAreSent(entries);
        verifyNoMoreInteractions(kafkaProducer);
        verify(highWaterMarkDAO).updateHighWaterMark(1);
    }

    @Test
    public void shouldSendMultipleEntriesWhenNotified() throws Exception {
        LogStream logStream = new LogStream(highWaterMarkDAO, entriesQueryDAO, kafkaProducer);

        ImmutableList<IndexedEntry> entries = ImmutableList.of(entry(1, "entry1"), entry(2, "entry2"));
        when(highWaterMarkDAO.getCurrentHighWaterMark()).thenReturn(0);
        whenEntriesAreInDatabase(0, entries);

        logStream.notifyOfNewEntries();

        verifyEntriesAreSent(entries);
        verifyNoMoreInteractions(kafkaProducer);
        verify(highWaterMarkDAO).updateHighWaterMark(2);
    }

    private IndexedEntry entry(int serial, String entry1) {
        return new IndexedEntry(serial, entry1.getBytes());
    }

    private OngoingStubbing<Iterator<IndexedEntry>> whenEntriesAreInDatabase(int since, List<IndexedEntry> indexedEntries) {
        return when(entriesQueryDAO.getEntriesSince(since)).thenReturn(indexedEntries.iterator());
    }

    private void verifyEntriesAreSent(List<IndexedEntry> entries) {
        ArgumentCaptor<ProducerRecord> captor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaProducer, times(entries.size())).send(captor.capture());
        List<ProducerRecord> allValues = captor.getAllValues();
        int index = 0;
        for (ProducerRecord value : allValues) {
            IndexedEntry indexedEntry = entries.get(index);
            assertThat(value.key(), equalTo(String.valueOf(indexedEntry.getSerial())));
            assertThat(value.topic(), equalTo(LogStream.TOPIC_NAME));
            assertThat(value.value(), equalTo(indexedEntry.getEntry()));
            index++;
        }
    }
}
