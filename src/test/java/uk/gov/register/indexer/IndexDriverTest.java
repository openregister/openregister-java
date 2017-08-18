package uk.gov.register.indexer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import uk.gov.register.core.*;
import uk.gov.register.indexer.function.IndexFunction;
import uk.gov.register.store.DataAccessLayer;
import uk.gov.register.util.HashValue;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.mockito.Mockito.*;

public class IndexDriverTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private DataAccessLayer dataAccessLayer;

    @Before
    public void setup() {
        dataAccessLayer = mock(DataAccessLayer.class);
    }

    @Test
    public void getStartIndices_shouldReturnEmptyList_whenNoNewPairsExist() {
        IndexDriver indexDriver = new IndexDriver();

        Set<IndexKeyItemPair> existingPairs = new HashSet<>(Arrays.asList(
                new IndexKeyItemPair("A", new HashValue(HashingAlgorithm.SHA256, "aaa")),
                new IndexKeyItemPair("B", new HashValue(HashingAlgorithm.SHA256, "bbb"))
        ));
        Set<IndexKeyItemPair> newPairs = new HashSet<>(Arrays.asList(
                new IndexKeyItemPair("A", new HashValue(HashingAlgorithm.SHA256, "aaa")),
                new IndexKeyItemPair("B", new HashValue(HashingAlgorithm.SHA256, "bbb"))
        ));

        List<IndexKeyItemPairEvent> pairsToStart = indexDriver.getStartIndices(existingPairs, newPairs);

        assertThat(pairsToStart, empty());
    }

    @Test
    public void getStartIndices_shouldReturnNewPairs_whenNewPairsExist() {
        IndexDriver indexDriver = new IndexDriver();

        Set<IndexKeyItemPair> existingPairs = new HashSet<>(Arrays.asList(
                new IndexKeyItemPair("A", new HashValue(HashingAlgorithm.SHA256, "aaa")),
                new IndexKeyItemPair("B", new HashValue(HashingAlgorithm.SHA256, "bbb"))
        ));
        Set<IndexKeyItemPair> newPairs = new HashSet<>(Arrays.asList(
                new IndexKeyItemPair("A", new HashValue(HashingAlgorithm.SHA256, "aaa")),
                new IndexKeyItemPair("B", new HashValue(HashingAlgorithm.SHA256, "bbb")),
                new IndexKeyItemPair("C", new HashValue(HashingAlgorithm.SHA256, "ccc"))
        ));

        List<IndexKeyItemPairEvent> pairsToStart = indexDriver.getStartIndices(existingPairs, newPairs);

        assertThat(pairsToStart, contains(new IndexKeyItemPairEvent(new IndexKeyItemPair("C", new HashValue(HashingAlgorithm.SHA256, "ccc")), true)));
    }

    @Test
    public void getEndIndices_shouldReturnEmptyList_whenNoNewPairsExist() {
        IndexDriver indexDriver = new IndexDriver();

        Set<IndexKeyItemPair> existingPairs = new HashSet<>(Arrays.asList(
                new IndexKeyItemPair("A", new HashValue(HashingAlgorithm.SHA256, "aaa")),
                new IndexKeyItemPair("B", new HashValue(HashingAlgorithm.SHA256, "bbb"))
        ));
        Set<IndexKeyItemPair> newPairs = new HashSet<>(Arrays.asList(
                new IndexKeyItemPair("B", new HashValue(HashingAlgorithm.SHA256, "bbb")),
                new IndexKeyItemPair("A", new HashValue(HashingAlgorithm.SHA256, "aaa"))
        ));

        List<IndexKeyItemPairEvent> pairsToEnd = indexDriver.getEndIndices(existingPairs, newPairs);

        assertThat(pairsToEnd, empty());
    }

    @Test
    public void getEndIndices_shouldReturnEndedPairs_whenEndedPairsExist() {
        IndexDriver indexDriver = new IndexDriver();

        Set<IndexKeyItemPair> existingPairs = new HashSet<>(Arrays.asList(
                new IndexKeyItemPair("A", new HashValue(HashingAlgorithm.SHA256, "aaa")),
                new IndexKeyItemPair("B", new HashValue(HashingAlgorithm.SHA256, "bbb"))
        ));
        Set<IndexKeyItemPair> newPairs = new HashSet<>(Arrays.asList(
                new IndexKeyItemPair("B", new HashValue(HashingAlgorithm.SHA256, "bbb"))
        ));

        List<IndexKeyItemPairEvent> pairsToEnd = indexDriver.getEndIndices(existingPairs, newPairs);

        assertThat(pairsToEnd, contains(new IndexKeyItemPairEvent(new IndexKeyItemPair("A", new HashValue(HashingAlgorithm.SHA256, "aaa")), false)));
    }

    @Test
    public void indexEntry_shouldStartIndex_whenNewPairsExist() throws IOException {
        Item itemP = new Item(new HashValue(HashingAlgorithm.SHA256, "aaa"), objectMapper.readTree("{\"x\":\"P\"}"));
        Item itemQ = new Item(new HashValue(HashingAlgorithm.SHA256, "bbb"), objectMapper.readTree("{\"x\":\"Q\"}"));

        Entry previousEntry = new Entry(1, new HashValue(HashingAlgorithm.SHA256, "aaa"), Instant.now(), "A", EntryType.user);
        Entry newEntry = new Entry(2, Arrays.asList(new HashValue(HashingAlgorithm.SHA256, "aaa"), new HashValue(HashingAlgorithm.SHA256, "bbb")), Instant.now(), "A", EntryType.user);

        when(dataAccessLayer.getItemBySha256(new HashValue(HashingAlgorithm.SHA256, "aaa"))).thenReturn(Optional.of(itemP));
        when(dataAccessLayer.getItemBySha256(new HashValue(HashingAlgorithm.SHA256, "bbb"))).thenReturn(Optional.of(itemQ));
        when(dataAccessLayer.getStartIndexEntryNumberAndExistingItemCount("by-x", "P", "bbb")).thenReturn(new IndexEntryNumberItemCountPair(Optional.empty(), 0));

        IndexFunction indexFunction = mock(IndexFunction.class);
        when(indexFunction.getName()).thenReturn("by-x");
        when(indexFunction.execute(ArgumentMatchers.any(), ArgumentMatchers.eq(previousEntry)))
                .thenReturn(new HashSet<>(Arrays.asList(new IndexKeyItemPair("P", new HashValue(HashingAlgorithm.SHA256, "aaa")))));
        when(indexFunction.execute(ArgumentMatchers.any(), ArgumentMatchers.eq(newEntry)))
                .thenReturn(new HashSet<>(Arrays.asList(new IndexKeyItemPair("P", new HashValue(HashingAlgorithm.SHA256, "aaa")), new IndexKeyItemPair("P", new HashValue(HashingAlgorithm.SHA256, "bbb")))));
        
        Map<String, Record> currentRecords = new HashMap<>();
        currentRecords.put("A", new Record(previousEntry, itemP));

        IndexDriver indexDriver = new IndexDriver();
        indexDriver.indexEntry(dataAccessLayer, newEntry, indexFunction, currentRecords, 1);

        verify(dataAccessLayer, times(1)).start("by-x", "P", "bbb", 2, 2);
        verify(dataAccessLayer, times(1)).start(anyString(), anyString(), anyString(), anyInt(), anyInt());
        verify(dataAccessLayer, never()).end(anyString(), anyString(), anyString(), anyString(), anyInt(), anyInt(), anyInt());
    }

    @Test
    public void indexEntry_shouldEndIndex_whenEndedPairExists() throws IOException {
        Item itemP = new Item(new HashValue(HashingAlgorithm.SHA256, "aaa"), objectMapper.readTree("{\"x\":\"P\"}"));
        Item itemQ = new Item(new HashValue(HashingAlgorithm.SHA256, "bbb"), objectMapper.readTree("{\"x\":\"Q\"}"));

        Entry previousEntry = new Entry(1, Arrays.asList(new HashValue(HashingAlgorithm.SHA256, "aaa"), new HashValue(HashingAlgorithm.SHA256, "bbb")), Instant.now(), "A", EntryType.user);
        Entry newEntry = new Entry(2, new HashValue(HashingAlgorithm.SHA256, "bbb"), Instant.now(), "A", EntryType.user);

        when(dataAccessLayer.getItemBySha256(new HashValue(HashingAlgorithm.SHA256, "bbb"))).thenReturn(Optional.of(itemQ));
        when(dataAccessLayer.getStartIndexEntryNumberAndExistingItemCount("by-x", "P", "aaa")).thenReturn(new IndexEntryNumberItemCountPair(Optional.of(1), 1));

        IndexFunction indexFunction = mock(IndexFunction.class);
        when(indexFunction.getName()).thenReturn("by-x");
        when(indexFunction.execute(ArgumentMatchers.any(), ArgumentMatchers.eq(previousEntry)))
                .thenReturn(new HashSet<>(Arrays.asList(new IndexKeyItemPair("P", new HashValue(HashingAlgorithm.SHA256, "aaa")), new IndexKeyItemPair("P", new HashValue(HashingAlgorithm.SHA256, "bbb")))));
        when(indexFunction.execute(ArgumentMatchers.any(), ArgumentMatchers.eq(newEntry)))
                .thenReturn(new HashSet<>(Arrays.asList(new IndexKeyItemPair("P", new HashValue(HashingAlgorithm.SHA256, "bbb")))));
        
        Map<String, Record> currentRecords = new HashMap<>();
        currentRecords.put("A", new Record(previousEntry, Arrays.asList(itemP, itemQ)));

        IndexDriver indexDriver = new IndexDriver();
        indexDriver.indexEntry(dataAccessLayer, newEntry, indexFunction, currentRecords, 2);

        verify(dataAccessLayer, times(1)).end("by-x", "A", "P", "aaa", 2, 3, 1);
        verify(dataAccessLayer, times(1)).end(anyString(), anyString(), anyString(), anyString(), anyInt(), anyInt(), anyInt());
        verify(dataAccessLayer, never()).start(anyString(), anyString(), anyString(), anyInt(), anyInt());
    }

    @Test
    public void indexEntry_shouldEndThenStartIndex_usingAlphabeticalKeyOrder_whenItemMovesFromOneGroupToAnother() throws IOException {
        Item itemP = new Item(new HashValue(HashingAlgorithm.SHA256, "aaa"), objectMapper.readTree("{\"x\":\"P\"}"));
        Item itemQ = new Item(new HashValue(HashingAlgorithm.SHA256, "bbb"), objectMapper.readTree("{\"x\":\"Q\"}"));

        Entry previousEntry = new Entry(1, new HashValue(HashingAlgorithm.SHA256, "aaa"), Instant.now(), "A", EntryType.user);
        Entry newEntry = new Entry(2, new HashValue(HashingAlgorithm.SHA256, "bbb"), Instant.now(), "A", EntryType.user);

        when(dataAccessLayer.getItemBySha256(new HashValue(HashingAlgorithm.SHA256, "bbb"))).thenReturn(Optional.of(itemQ));
        when(dataAccessLayer.getStartIndexEntryNumberAndExistingItemCount("by-x", "P", "aaa")).thenReturn(new IndexEntryNumberItemCountPair(Optional.of(1), 1));
        when(dataAccessLayer.getStartIndexEntryNumberAndExistingItemCount("by-x", "Q", "bbb")).thenReturn(new IndexEntryNumberItemCountPair(Optional.empty(), 0));

        IndexFunction indexFunction = mock(IndexFunction.class);
        when(indexFunction.getName()).thenReturn("by-x");
        when(indexFunction.execute(ArgumentMatchers.any(), ArgumentMatchers.eq(previousEntry)))
                .thenReturn(new HashSet<>(Arrays.asList(new IndexKeyItemPair("P", new HashValue(HashingAlgorithm.SHA256, "aaa")))));
        when(indexFunction.execute(ArgumentMatchers.any(), ArgumentMatchers.eq(newEntry)))
                .thenReturn(new HashSet<>(Arrays.asList(new IndexKeyItemPair("Q", new HashValue(HashingAlgorithm.SHA256, "bbb")))));

        Map<String, Record> currentRecords = new HashMap<>();
        currentRecords.put("A", new Record(previousEntry, itemP));
        
        IndexDriver indexDriver = new IndexDriver();
        indexDriver.indexEntry(dataAccessLayer, newEntry, indexFunction, currentRecords, 1);

        verify(dataAccessLayer, times(1)).end("by-x", "A", "P", "aaa", 2, 2, 1);
        verify(dataAccessLayer, times(1)).start("by-x", "Q", "bbb", 2, 3);
        verify(dataAccessLayer, times(1)).end(anyString(), anyString(), anyString(), anyString(), anyInt(), anyInt(), anyInt());
        verify(dataAccessLayer, times(1)).start(anyString(), anyString(), anyString(), anyInt(), anyInt());
    }

    @Test
    public void indexEntry_shouldStartThenEndIndex_usingAlphabeticalKeyOrder_whenItemMovesFromOneGroupToAnother() throws IOException {
        Item itemQ = new Item(new HashValue(HashingAlgorithm.SHA256, "bbb"), objectMapper.readTree("{\"x\":\"Q\"}"));
        Item itemP = new Item(new HashValue(HashingAlgorithm.SHA256, "aaa"), objectMapper.readTree("{\"x\":\"P\"}"));

        Entry previousEntry = new Entry(1, new HashValue(HashingAlgorithm.SHA256, "bbb"), Instant.now(), "A", EntryType.user);
        Entry newEntry = new Entry(2, new HashValue(HashingAlgorithm.SHA256, "aaa"), Instant.now(), "A", EntryType.user);

        when(dataAccessLayer.getItemBySha256(new HashValue(HashingAlgorithm.SHA256, "aaa"))).thenReturn(Optional.of(itemP));

        IndexFunction indexFunction = mock(IndexFunction.class);
        when(indexFunction.getName()).thenReturn("by-x");
        when(indexFunction.execute(ArgumentMatchers.any(), ArgumentMatchers.eq(previousEntry)))
                .thenReturn(new HashSet<>(Arrays.asList(new IndexKeyItemPair("Q", new HashValue(HashingAlgorithm.SHA256, "bbb")))));
        when(indexFunction.execute(ArgumentMatchers.any(), ArgumentMatchers.eq(newEntry)))
                .thenReturn(new HashSet<>(Arrays.asList(new IndexKeyItemPair("P", new HashValue(HashingAlgorithm.SHA256, "aaa")))));

        when(dataAccessLayer.getStartIndexEntryNumberAndExistingItemCount("by-x", "Q", "bbb")).thenReturn(new IndexEntryNumberItemCountPair(Optional.of(1), 1));
        when(dataAccessLayer.getStartIndexEntryNumberAndExistingItemCount("by-x", "P", "aaa")).thenReturn(new IndexEntryNumberItemCountPair(Optional.empty(), 0));

        Map<String, Record> currentRecords = new HashMap<>();
        currentRecords.put("A", new Record(previousEntry, itemQ));
        
        IndexDriver indexDriver = new IndexDriver();
        indexDriver.indexEntry(dataAccessLayer, newEntry, indexFunction, currentRecords, 0);

        verify(dataAccessLayer, times(1)).start("by-x", "P", "aaa", 2, 1);
        verify(dataAccessLayer, times(1)).end("by-x", "A", "Q", "bbb", 2, 2, 1);
        verify(dataAccessLayer, times(1)).start(anyString(), anyString(), anyString(), anyInt(), anyInt());
        verify(dataAccessLayer, times(1)).end(anyString(), anyString(), anyString(), anyString(), anyInt(), anyInt(), anyInt());
    }

    @Test
    public void indexEntry_shouldEndAndStartIndex_whenItemChangesWithoutMovingGroup() throws IOException {
        Item itemP = new Item(new HashValue(HashingAlgorithm.SHA256, "aaa"), objectMapper.readTree("{\"x\":\"P\",\"y\":\"S\"}"));
        Item itemQ = new Item(new HashValue(HashingAlgorithm.SHA256, "bbb"), objectMapper.readTree("{\"x\":\"P\",\"y\":\"T\"}"));

        Entry previousEntry = new Entry(1, new HashValue(HashingAlgorithm.SHA256, "aaa"), Instant.now(), "A", EntryType.user);
        Entry newEntry = new Entry(2, new HashValue(HashingAlgorithm.SHA256, "bbb"), Instant.now(), "A", EntryType.user);

        when(dataAccessLayer.getItemBySha256(new HashValue(HashingAlgorithm.SHA256, "bbb"))).thenReturn(Optional.of(itemQ));
        when(dataAccessLayer.getStartIndexEntryNumberAndExistingItemCount("by-x", "P", "aaa")).thenReturn(new IndexEntryNumberItemCountPair(Optional.of(1), 1));
        when(dataAccessLayer.getStartIndexEntryNumberAndExistingItemCount("by-x", "P", "bbb")).thenReturn(new IndexEntryNumberItemCountPair(Optional.empty(), 0));

        IndexFunction indexFunction = mock(IndexFunction.class);
        when(indexFunction.getName()).thenReturn("by-x");
        when(indexFunction.execute(ArgumentMatchers.any(), ArgumentMatchers.eq(previousEntry)))
                .thenReturn(new HashSet<>(Arrays.asList(new IndexKeyItemPair("P", new HashValue(HashingAlgorithm.SHA256, "aaa")))));
        when(indexFunction.execute(ArgumentMatchers.any(), ArgumentMatchers.eq(newEntry)))
                .thenReturn(new HashSet<>(Arrays.asList(new IndexKeyItemPair("P", new HashValue(HashingAlgorithm.SHA256, "bbb")))));
        
        Map<String, Record> currentRecords = new HashMap<>();
        currentRecords.put("A", new Record(previousEntry, itemP));
        
        IndexDriver indexDriver = new IndexDriver();
        indexDriver.indexEntry(dataAccessLayer, newEntry, indexFunction, currentRecords, 1);

        verify(dataAccessLayer, times(1)).end("by-x", "A", "P", "aaa", 2, 2, 1);
        verify(dataAccessLayer, times(1)).start("by-x", "P", "bbb", 2, 2);
        verify(dataAccessLayer, times(1)).end(anyString(), anyString(), anyString(), anyString(), anyInt(), anyInt(), anyInt());
        verify(dataAccessLayer, times(1)).start(anyString(), anyString(), anyString(), anyInt(), anyInt());
    }

    @Test
    public void indexEntry_shouldStartIndexesWithIncreasingIndexNumbers_whenAddingNewItems() throws IOException {
        Item itemP = new Item(new HashValue(HashingAlgorithm.SHA256, "aaa"), objectMapper.readTree("{\"x\":\"P\"}"));
        Item itemQ = new Item(new HashValue(HashingAlgorithm.SHA256, "bbb"), objectMapper.readTree("{\"x\":\"Q\"}"));

        Entry newEntry1 = new Entry(1, new HashValue(HashingAlgorithm.SHA256, "aaa"), Instant.now(), "A", EntryType.user);
        Entry newEntry2 = new Entry(2, new HashValue(HashingAlgorithm.SHA256, "bbb"), Instant.now(), "B", EntryType.user);

        when(dataAccessLayer.getItemBySha256(new HashValue(HashingAlgorithm.SHA256, "aaa"))).thenReturn(Optional.of(itemP));
        when(dataAccessLayer.getItemBySha256(new HashValue(HashingAlgorithm.SHA256, "bbb"))).thenReturn(Optional.of(itemQ));
        when(dataAccessLayer.getStartIndexEntryNumberAndExistingItemCount("by-x", "P", "aaa")).thenReturn(new IndexEntryNumberItemCountPair(Optional.empty(), 0));
        when(dataAccessLayer.getStartIndexEntryNumberAndExistingItemCount("by-x", "Q", "bbb")).thenReturn(new IndexEntryNumberItemCountPair(Optional.empty(), 0));

        IndexFunction indexFunction = mock(IndexFunction.class);
        when(indexFunction.getName()).thenReturn("by-x");
        when(indexFunction.execute(ArgumentMatchers.any(), ArgumentMatchers.eq(newEntry1)))
                .thenReturn(new HashSet<>(Arrays.asList(new IndexKeyItemPair("P", new HashValue(HashingAlgorithm.SHA256, "aaa")))));
        when(indexFunction.execute(ArgumentMatchers.any(), ArgumentMatchers.eq(newEntry2)))
                .thenReturn(new HashSet<>(Arrays.asList(new IndexKeyItemPair("Q", new HashValue(HashingAlgorithm.SHA256, "bbb")))));

        IndexDriver indexDriver = new IndexDriver();
        indexDriver.indexEntry(dataAccessLayer, newEntry1, indexFunction, new HashMap<>(), 0);
        indexDriver.indexEntry(dataAccessLayer, newEntry2, indexFunction, new HashMap<>(), 1);

        verify(dataAccessLayer, times(1)).start("by-x", "P", "aaa", 1, 1);
        verify(dataAccessLayer, times(1)).start("by-x", "Q", "bbb", 2, 2);
        verify(dataAccessLayer, times(2)).start(anyString(), anyString(), anyString(), anyInt(), anyInt());
        verify(dataAccessLayer, times(0)).end(anyString(), anyString(), anyString(), anyString(), anyInt(), anyInt(), anyInt());
    }

    @Test
    public void indexEntry_shouldNotSkipStartIndexEntryNumber_whenAddingMultipleItemsContainingDuplicates() throws IOException {
        Item itemP = new Item(new HashValue(HashingAlgorithm.SHA256, "aaa"), objectMapper.readTree("{\"x\":\"P\"}"));
        Item itemQ = new Item(new HashValue(HashingAlgorithm.SHA256, "bbb"), objectMapper.readTree("{\"x\":\"Q\"}"));
        Item itemR = new Item(new HashValue(HashingAlgorithm.SHA256, "ccc"), objectMapper.readTree("{\"x\":\"R\"}"));
        Item itemS = new Item(new HashValue(HashingAlgorithm.SHA256, "ddd"), objectMapper.readTree("{\"x\":\"S\"}"));

        Entry newEntry1 = new Entry(1, new HashValue(HashingAlgorithm.SHA256, "aaa"), Instant.now(), "A", EntryType.user);
        Entry newEntry2 = new Entry(2, new HashValue(HashingAlgorithm.SHA256, "bbb"), Instant.now(), "B", EntryType.user);
        Entry newEntry3 = new Entry(3, new HashValue(HashingAlgorithm.SHA256, "ddd"), Instant.now(), "C", EntryType.user);
        Entry newEntry4 = new Entry(4, new HashValue(HashingAlgorithm.SHA256, "bbb"), Instant.now(), "C", EntryType.user);
        Entry newEntry5 = new Entry(5, new HashValue(HashingAlgorithm.SHA256, "ccc"), Instant.now(), "D", EntryType.user);

        when(dataAccessLayer.getItemBySha256(new HashValue(HashingAlgorithm.SHA256, "aaa"))).thenReturn(Optional.of(itemP));
        when(dataAccessLayer.getItemBySha256(new HashValue(HashingAlgorithm.SHA256, "bbb"))).thenReturn(Optional.of(itemQ));
        when(dataAccessLayer.getItemBySha256(new HashValue(HashingAlgorithm.SHA256, "ccc"))).thenReturn(Optional.of(itemR));
        when(dataAccessLayer.getItemBySha256(new HashValue(HashingAlgorithm.SHA256, "ddd"))).thenReturn(Optional.of(itemS));
        when(dataAccessLayer.getStartIndexEntryNumberAndExistingItemCount("by-x", "Q", "bbb")).thenReturn(new IndexEntryNumberItemCountPair(Optional.empty(), 0), new IndexEntryNumberItemCountPair(Optional.of(2), 1));
        when(dataAccessLayer.getStartIndexEntryNumberAndExistingItemCount("by-x", "P", "aaa")).thenReturn(new IndexEntryNumberItemCountPair(Optional.empty(), 0));
        when(dataAccessLayer.getStartIndexEntryNumberAndExistingItemCount("by-x", "S", "ddd")).thenReturn(new IndexEntryNumberItemCountPair(Optional.empty(), 0), new IndexEntryNumberItemCountPair(Optional.of(3), 1));
        when(dataAccessLayer.getStartIndexEntryNumberAndExistingItemCount("by-x", "R", "ccc")).thenReturn(new IndexEntryNumberItemCountPair(Optional.empty(), 0));

        IndexFunction indexFunction = mock(IndexFunction.class);
        when(indexFunction.getName()).thenReturn("by-x");
        when(indexFunction.execute(ArgumentMatchers.any(), ArgumentMatchers.eq(newEntry1)))
                .thenReturn(new HashSet<>(Arrays.asList(new IndexKeyItemPair("P", new HashValue(HashingAlgorithm.SHA256, "aaa")))));
        when(indexFunction.execute(ArgumentMatchers.any(), ArgumentMatchers.eq(newEntry2)))
                .thenReturn(new HashSet<>(Arrays.asList(new IndexKeyItemPair("Q", new HashValue(HashingAlgorithm.SHA256, "bbb")))));
        when(indexFunction.execute(ArgumentMatchers.any(), ArgumentMatchers.eq(newEntry3)))
                .thenReturn(new HashSet<>(Arrays.asList(new IndexKeyItemPair("S", new HashValue(HashingAlgorithm.SHA256, "ddd")))));
        when(indexFunction.execute(ArgumentMatchers.any(), ArgumentMatchers.eq(newEntry4)))
                .thenReturn(new HashSet<>(Arrays.asList(new IndexKeyItemPair("Q", new HashValue(HashingAlgorithm.SHA256, "bbb")))));
        when(indexFunction.execute(ArgumentMatchers.any(), ArgumentMatchers.eq(newEntry5)))
                .thenReturn(new HashSet<>(Arrays.asList(new IndexKeyItemPair("R", new HashValue(HashingAlgorithm.SHA256, "ccc")))));

        Map<String, Record> currentRecordsAtEntry4 = new HashMap<>();
        currentRecordsAtEntry4.put("C", new Record(newEntry3, itemS));
        
        IndexDriver indexDriver = new IndexDriver();
        indexDriver.indexEntry(dataAccessLayer, newEntry1, indexFunction, new HashMap<>(), 0);
        indexDriver.indexEntry(dataAccessLayer, newEntry2, indexFunction, new HashMap<>(), 1);
        indexDriver.indexEntry(dataAccessLayer, newEntry3, indexFunction, new HashMap<>(), 2);
        indexDriver.indexEntry(dataAccessLayer, newEntry4, indexFunction, currentRecordsAtEntry4, 3);
        indexDriver.indexEntry(dataAccessLayer, newEntry5, indexFunction, new HashMap<>(), 4);

        verify(dataAccessLayer, times(1)).start("by-x", "P", "aaa", 1, 1);
        verify(dataAccessLayer, times(1)).start("by-x", "Q", "bbb", 2, 2);
        verify(dataAccessLayer, times(1)).start("by-x", "S", "ddd", 3, 3);
        verify(dataAccessLayer, times(1)).start("by-x", "Q", "bbb", 4, 2);
        verify(dataAccessLayer, times(1)).end("by-x", "C", "S", "ddd", 4, 4, 3);
        verify(dataAccessLayer, times(1)).start("by-x", "R", "ccc", 5, 5);

        verify(dataAccessLayer, times(5)).start(anyString(), anyString(), anyString(), anyInt(), anyInt());
        verify(dataAccessLayer, times(1)).end(anyString(), anyString(), anyString(), anyString(), anyInt(), anyInt(), anyInt());
    }

    @Test
    public void indexEntry_shouldStartIndexSpecifyingStartIndexEntryNumber_whenItemExistsUnderAnotherIndex() throws IOException {
        Item item = new Item(new HashValue(HashingAlgorithm.SHA256, "aaa"), objectMapper.readTree("{\"x\":\"P\"}"));

        Entry previousEntry = new Entry(1, new HashValue(HashingAlgorithm.SHA256, "aaa"), Instant.now(), "A", EntryType.user);
        Entry newEntry = new Entry(2, new HashValue(HashingAlgorithm.SHA256, "aaa"), Instant.now(), "B", EntryType.user);
        
        when(dataAccessLayer.getItemBySha256(new HashValue(HashingAlgorithm.SHA256, "aaa"))).thenReturn(Optional.of(item));
        when(dataAccessLayer.getStartIndexEntryNumberAndExistingItemCount("by-x", "P", "aaa")).thenReturn(new IndexEntryNumberItemCountPair(Optional.of(1), 1));
        IndexFunction indexFunction = mock(IndexFunction.class);
        when(indexFunction.getName()).thenReturn("by-x");
        when(indexFunction.execute(ArgumentMatchers.any(), ArgumentMatchers.eq(newEntry)))
                .thenReturn(new HashSet<>(Arrays.asList(new IndexKeyItemPair("P", new HashValue(HashingAlgorithm.SHA256, "aaa")))));

        IndexDriver indexDriver = new IndexDriver();
        indexDriver.indexEntry(dataAccessLayer, newEntry, indexFunction, new HashMap<>(), 1);

        verify(dataAccessLayer, times(1)).start("by-x", "P", "aaa", 2, 1);
        verify(dataAccessLayer, times(1)).start(anyString(), anyString(), anyString(), anyInt(), anyInt());
        verify(dataAccessLayer, times(0)).end(anyString(), anyString(), anyString(), anyString(), anyInt(), anyInt(), anyInt());
    }

    @Test
    public void indexEntry_shouldEndIndexUsingNewIndexEntryNumber_whenItemExistsUnderAnExistingSingleIndex() throws IOException {
        Item item = new Item(new HashValue(HashingAlgorithm.SHA256, "aaa"), objectMapper.readTree("{\"x\":\"P\"}"));

        Entry previousEntry = new Entry(2, new HashValue(HashingAlgorithm.SHA256, "aaa"), Instant.now(), "B", EntryType.user);
        Entry newEntry = new Entry(3, new HashValue(HashingAlgorithm.SHA256, "aaa"), Instant.now(), "B", EntryType.user);

        when(dataAccessLayer.getItemBySha256(new HashValue(HashingAlgorithm.SHA256, "aaa"))).thenReturn(Optional.of(item));
        when(dataAccessLayer.getStartIndexEntryNumberAndExistingItemCount("by-x", "P", "aaa")).thenReturn(new IndexEntryNumberItemCountPair(Optional.of(2), 1));
        IndexFunction indexFunction = mock(IndexFunction.class);
        when(indexFunction.getName()).thenReturn("by-x");
        when(indexFunction.execute(ArgumentMatchers.any(), ArgumentMatchers.eq(previousEntry)))
                .thenReturn(new HashSet<>(Arrays.asList(new IndexKeyItemPair("P", new HashValue(HashingAlgorithm.SHA256, "aaa")))));
        when(indexFunction.execute(ArgumentMatchers.any(), ArgumentMatchers.eq(newEntry)))
                .thenReturn(new HashSet<>());
        
        Map<String, Record> currentRecords = new HashMap<>();
        currentRecords.put("B", new Record(previousEntry, item));

        IndexDriver indexDriver = new IndexDriver();
        indexDriver.indexEntry(dataAccessLayer, newEntry, indexFunction, currentRecords, 2);

        verify(dataAccessLayer, times(1)).end("by-x", "B", "P", "aaa", 3, 3, 2);
        verify(dataAccessLayer, times(1)).end(anyString(), anyString(), anyString(), anyString(), anyInt(), anyInt(), anyInt());
        verify(dataAccessLayer, times(0)).start(anyString(), anyString(), anyString(), anyInt(), anyInt());
    }
    
    @Test
    public void indexEntry_shouldEndIndexUsingStartEntryNumber_whenItemExistsUnderMultipleExistingIndexes() throws IOException {
        Item itemP = new Item(new HashValue(HashingAlgorithm.SHA256, "aaa"), objectMapper.readTree("{\"x\":\"P\"}"));
        Item itemQ = new Item(new HashValue(HashingAlgorithm.SHA256, "bbb"), objectMapper.readTree("{\"x\":\"Q\"}"));

        Entry previousEntry1 = new Entry(1, new HashValue(HashingAlgorithm.SHA256, "aaa"), Instant.now(), "B", EntryType.user);
        Entry previousEntry2 = new Entry(2, new HashValue(HashingAlgorithm.SHA256, "aaa"), Instant.now(), "C", EntryType.user);
        Entry previousEntry3 = new Entry(3, new HashValue(HashingAlgorithm.SHA256, "aaa"), Instant.now(), "D", EntryType.user);
        Entry newEntry = new Entry(4, new HashValue(HashingAlgorithm.SHA256, "bbb"), Instant.now(), "B", EntryType.user);

        when(dataAccessLayer.getItemBySha256(new HashValue(HashingAlgorithm.SHA256, "aaa"))).thenReturn(Optional.of(itemP));
        when(dataAccessLayer.getItemBySha256(new HashValue(HashingAlgorithm.SHA256, "bbb"))).thenReturn(Optional.of(itemQ));
        when(dataAccessLayer.getStartIndexEntryNumberAndExistingItemCount("by-x", "P", "aaa")).thenReturn(new IndexEntryNumberItemCountPair(Optional.of(1), 3));
        when(dataAccessLayer.getStartIndexEntryNumberAndExistingItemCount("by-x", "Q", "bbb")).thenReturn(new IndexEntryNumberItemCountPair(Optional.empty(), 0));
        IndexFunction indexFunction = mock(IndexFunction.class);
        when(indexFunction.getName()).thenReturn("by-x");
        when(indexFunction.execute(ArgumentMatchers.any(), ArgumentMatchers.eq(previousEntry1)))
                .thenReturn(new HashSet<>(Arrays.asList(new IndexKeyItemPair("P", new HashValue(HashingAlgorithm.SHA256, "aaa")))));
        when(indexFunction.execute(ArgumentMatchers.any(), ArgumentMatchers.eq(newEntry)))
                .thenReturn(new HashSet<>(Arrays.asList(new IndexKeyItemPair("Q", new HashValue(HashingAlgorithm.SHA256, "bbb")))));

        Map<String, Record> currentRecords = new HashMap<>();
        currentRecords.put("B", new Record(previousEntry1, itemP));

        IndexDriver indexDriver = new IndexDriver();
        indexDriver.indexEntry(dataAccessLayer, newEntry, indexFunction, currentRecords, 1);

        verify(dataAccessLayer, times(1)).end("by-x", "B", "P", "aaa", 4, 1, 1);
        verify(dataAccessLayer, times(1)).start("by-x", "Q", "bbb", 4, 2);
        verify(dataAccessLayer, times(1)).end(anyString(), anyString(), anyString(), anyString(), anyInt(), anyInt(), anyInt());
        verify(dataAccessLayer, times(1)).start(anyString(), anyString(), anyString(), anyInt(), anyInt());
    }
}
