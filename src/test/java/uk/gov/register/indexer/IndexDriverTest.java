package uk.gov.register.indexer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import uk.gov.register.core.*;
import uk.gov.register.db.IndexDAO;
import uk.gov.register.db.IndexQueryDAO;
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
    private Register register;
    private DataAccessLayer dataAccessLayer;

    @Before
    public void setup() {
        register = mock(Register.class);
        dataAccessLayer = mock(DataAccessLayer.class);
    }

    @Test
    public void getStartIndices_shouldReturnEmptyList_whenNoNewPairsExist() {
        IndexDriver indexDriver = new IndexDriver(dataAccessLayer);

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
        IndexDriver indexDriver = new IndexDriver(dataAccessLayer);

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

        IndexDriver indexDriver = new IndexDriver(dataAccessLayer);

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
        IndexDriver indexDriver = new IndexDriver(dataAccessLayer);

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
        Item item = new Item(new HashValue(HashingAlgorithm.SHA256, "aaa"), objectMapper.readTree("{\"x\":\"P\"}"));

        Entry previousEntry = new Entry(1, new HashValue(HashingAlgorithm.SHA256, "aaa"), Instant.now(), "A", EntryType.user);
        Entry newEntry = new Entry(2, Arrays.asList(new HashValue(HashingAlgorithm.SHA256, "aaa"), new HashValue(HashingAlgorithm.SHA256, "bbb")), Instant.now(), "A", EntryType.user);


        when(register.getRecord("A")).thenReturn(Optional.of(new Record(previousEntry, item)));

        IndexDAO indexDAO = mock(IndexDAO.class);
        IndexQueryDAO indexQueryDAO = mock(IndexQueryDAO.class);
        when(indexQueryDAO.getCurrentIndexEntryNumber("by-x", "x")).thenReturn(0);
        IndexFunction indexFunction = mock(IndexFunction.class);
        when(indexFunction.getName()).thenReturn("by-x");
        when(indexFunction.execute(register, previousEntry))
                .thenReturn(new HashSet<>(Arrays.asList(new IndexKeyItemPair("P", new HashValue(HashingAlgorithm.SHA256, "aaa")))));
        when(indexFunction.execute(register, newEntry))
                .thenReturn(new HashSet<>(Arrays.asList(new IndexKeyItemPair("P", new HashValue(HashingAlgorithm.SHA256, "aaa")), new IndexKeyItemPair("P", new HashValue(HashingAlgorithm.SHA256, "bbb")))));

        IndexDriver indexDriver = new IndexDriver(dataAccessLayer);
        indexDriver.indexEntry(register, newEntry, indexFunction);

        verify(dataAccessLayer, times(1)).start("by-x", "P", "bbb", 2, 1);
        verifyNoMoreInteractions(indexDAO);
    }

    @Test
    public void indexEntry_shouldEndIndex_whenEndedPairExists() throws IOException {
        Item item = new Item(new HashValue(HashingAlgorithm.SHA256, "aaa"), objectMapper.readTree("{\"x\":\"P\"}"));

        Entry previousEntry = new Entry(1, Arrays.asList(new HashValue(HashingAlgorithm.SHA256, "aaa"), new HashValue(HashingAlgorithm.SHA256, "bbb")), Instant.now(), "A", EntryType.user);
        Entry newEntry = new Entry(2, new HashValue(HashingAlgorithm.SHA256, "bbb"), Instant.now(), "A", EntryType.user);


        when(register.getRecord("A")).thenReturn(Optional.of(new Record(previousEntry, item)));

        IndexDAO indexDAO = mock(IndexDAO.class);
        IndexQueryDAO indexQueryDAO = mock(IndexQueryDAO.class);
        when(indexQueryDAO.getCurrentIndexEntryNumber("by-x", "x")).thenReturn(0);
        IndexFunction indexFunction = mock(IndexFunction.class);
        when(indexFunction.getName()).thenReturn("by-x");
        when(indexFunction.execute(register, previousEntry))
                .thenReturn(new HashSet<>(Arrays.asList(new IndexKeyItemPair("P", new HashValue(HashingAlgorithm.SHA256, "aaa")), new IndexKeyItemPair("P", new HashValue(HashingAlgorithm.SHA256, "bbb")))));
        when(indexFunction.execute(register, newEntry))
                .thenReturn(new HashSet<>(Arrays.asList(new IndexKeyItemPair("P", new HashValue(HashingAlgorithm.SHA256, "bbb")))));

        IndexDriver indexDriver = new IndexDriver(dataAccessLayer);
        indexDriver.indexEntry(register, newEntry, indexFunction);

        verify(dataAccessLayer, times(1)).end("by-x", "A", "P", "aaa", 2, 1);
        verifyNoMoreInteractions(indexDAO);
    }

    @Test
    public void indexEntry_shouldEndThenStartIndex_usingAlphabeticalKeyOrder_whenItemMovesFromOneGroupToAnother() throws IOException {
        Item itemP = new Item(new HashValue(HashingAlgorithm.SHA256, "aaa"), objectMapper.readTree("{\"x\":\"P\"}"));
        Item itemQ = new Item(new HashValue(HashingAlgorithm.SHA256, "bbb"), objectMapper.readTree("{\"x\":\"Q\"}"));

        Entry previousEntry = new Entry(1, new HashValue(HashingAlgorithm.SHA256, "aaa"), Instant.now(), "A", EntryType.user);
        Entry newEntry = new Entry(2, new HashValue(HashingAlgorithm.SHA256, "bbb"), Instant.now(), "A", EntryType.user);


        when(register.getRecord("A")).thenReturn(Optional.of(new Record(previousEntry, itemP)));

        IndexDAO indexDAO = mock(IndexDAO.class);
        IndexQueryDAO indexQueryDAO = mock(IndexQueryDAO.class);
        when(indexQueryDAO.getCurrentIndexEntryNumber("by-x", "x")).thenReturn(0);
        IndexFunction indexFunction = mock(IndexFunction.class);
        when(indexFunction.getName()).thenReturn("by-x");
        when(indexFunction.execute(register, previousEntry))
                .thenReturn(new HashSet<>(Arrays.asList(new IndexKeyItemPair("P", new HashValue(HashingAlgorithm.SHA256, "aaa")))));
        when(indexFunction.execute(register, newEntry))
                .thenReturn(new HashSet<>(Arrays.asList(new IndexKeyItemPair("Q", new HashValue(HashingAlgorithm.SHA256, "bbb")))));


        IndexDriver indexDriver = new IndexDriver(dataAccessLayer);
        indexDriver.indexEntry(register, newEntry, indexFunction);

        verify(dataAccessLayer, times(1)).end("by-x", "A", "P", "aaa", 2, 1);
        verify(dataAccessLayer, times(1)).start("by-x", "Q", "bbb", 2, 2);
        verifyNoMoreInteractions(indexDAO);
    }

    @Test
    public void indexEntry_shouldStartThenEndIndex_usingAlphabeticalKeyOrder_whenItemMovesFromOneGroupToAnother() throws IOException {
        Item itemQ = new Item(new HashValue(HashingAlgorithm.SHA256, "bbb"), objectMapper.readTree("{\"x\":\"Q\"}"));
        Item itemP = new Item(new HashValue(HashingAlgorithm.SHA256, "aaa"), objectMapper.readTree("{\"x\":\"P\"}"));

        Entry previousEntry = new Entry(1, new HashValue(HashingAlgorithm.SHA256, "bbb"), Instant.now(), "A", EntryType.user);
        Entry newEntry = new Entry(2, new HashValue(HashingAlgorithm.SHA256, "aaa"), Instant.now(), "A", EntryType.user);


        when(register.getRecord("A")).thenReturn(Optional.of(new Record(previousEntry, itemQ)));

        IndexDAO indexDAO = mock(IndexDAO.class);
        IndexQueryDAO indexQueryDAO = mock(IndexQueryDAO.class);
        when(indexQueryDAO.getCurrentIndexEntryNumber("by-x", "x")).thenReturn(0);
        IndexFunction indexFunction = mock(IndexFunction.class);
        when(indexFunction.getName()).thenReturn("by-x");
        when(indexFunction.execute(register, previousEntry))
                .thenReturn(new HashSet<>(Arrays.asList(new IndexKeyItemPair("Q", new HashValue(HashingAlgorithm.SHA256, "bbb")))));
        when(indexFunction.execute(register, newEntry))
                .thenReturn(new HashSet<>(Arrays.asList(new IndexKeyItemPair("P", new HashValue(HashingAlgorithm.SHA256, "aaa")))));


        IndexDriver indexDriver = new IndexDriver(dataAccessLayer);
        indexDriver.indexEntry(register, newEntry, indexFunction);

        verify(dataAccessLayer, times(1)).start("by-x", "P", "aaa", 2, 1);
        verify(dataAccessLayer, times(1)).end("by-x", "A", "Q", "bbb", 2, 2);
        verifyNoMoreInteractions(indexDAO);
    }

    @Test
    public void indexEntry_shouldEndAndStartIndex_whenItemChangesWithoutMovingGroup() throws IOException {
        Item itemP = new Item(new HashValue(HashingAlgorithm.SHA256, "aaa"), objectMapper.readTree("{\"x\":\"P\",\"y\":\"S\"}"));
        Item itemQ = new Item(new HashValue(HashingAlgorithm.SHA256, "bbb"), objectMapper.readTree("{\"x\":\"P\",\"y\":\"T\"}"));

        Entry previousEntry = new Entry(1, new HashValue(HashingAlgorithm.SHA256, "aaa"), Instant.now(), "A", EntryType.user);
        Entry newEntry = new Entry(2, new HashValue(HashingAlgorithm.SHA256, "bbb"), Instant.now(), "A", EntryType.user);


        when(register.getRecord("A")).thenReturn(Optional.of(new Record(previousEntry, itemP)));

        IndexDAO indexDAO = mock(IndexDAO.class);
        IndexQueryDAO indexQueryDAO = mock(IndexQueryDAO.class);
        when(indexQueryDAO.getCurrentIndexEntryNumber("by-x", "x")).thenReturn(0);
        IndexFunction indexFunction = mock(IndexFunction.class);
        when(indexFunction.getName()).thenReturn("by-x");
        when(indexFunction.execute(register, previousEntry))
                .thenReturn(new HashSet<>(Arrays.asList(new IndexKeyItemPair("P", new HashValue(HashingAlgorithm.SHA256, "aaa")))));
        when(indexFunction.execute(register, newEntry))
                .thenReturn(new HashSet<>(Arrays.asList(new IndexKeyItemPair("P", new HashValue(HashingAlgorithm.SHA256, "bbb")))));


        IndexDriver indexDriver = new IndexDriver(dataAccessLayer);
        indexDriver.indexEntry(register, newEntry, indexFunction);

        verify(dataAccessLayer, times(1)).end("by-x", "A", "P", "aaa", 2, 1);
        verify(dataAccessLayer, times(1)).start("by-x", "P", "bbb", 2, 2);
        verifyNoMoreInteractions(indexDAO);
    }

    @Test
    public void indexEntry_shouldStartIndexesWithIncreasingIndexNumbers_whenAddingNewItems() throws IOException {
        Item itemP = new Item(new HashValue(HashingAlgorithm.SHA256, "aaa"), objectMapper.readTree("{\"x\":\"P\"}"));
        Item itemQ = new Item(new HashValue(HashingAlgorithm.SHA256, "bbb"), objectMapper.readTree("{\"x\":\"Q\"}"));

        Entry newEntry1 = new Entry(1, new HashValue(HashingAlgorithm.SHA256, "aaa"), Instant.now(), "A", EntryType.user);
        Entry newEntry2 = new Entry(2, new HashValue(HashingAlgorithm.SHA256, "bbb"), Instant.now(), "B", EntryType.user);


        when(register.getRecord(anyString())).thenReturn(Optional.empty());

        when(dataAccessLayer.getCurrentIndexEntryNumber("by-x")).thenReturn(0, 1);
        IndexFunction indexFunction = mock(IndexFunction.class);
        when(indexFunction.getName()).thenReturn("by-x");
        when(indexFunction.execute(register, newEntry1))
                .thenReturn(new HashSet<>(Arrays.asList(new IndexKeyItemPair("P", new HashValue(HashingAlgorithm.SHA256, "aaa")))));
        when(indexFunction.execute(register, newEntry2))
                .thenReturn(new HashSet<>(Arrays.asList(new IndexKeyItemPair("Q", new HashValue(HashingAlgorithm.SHA256, "bbb")))));

        IndexDriver indexDriver = new IndexDriver(dataAccessLayer);
        indexDriver.indexEntry(register, newEntry1, indexFunction);
        indexDriver.indexEntry(register, newEntry2, indexFunction);

        verify(dataAccessLayer, times(1)).start("by-x", "P", "aaa", 1, 1);
        verify(dataAccessLayer, times(1)).start("by-x", "Q", "bbb", 2, 2);
        verify(dataAccessLayer, times(2)).start(anyString(), anyString(), anyString(), anyInt(), any());
        verify(dataAccessLayer, times(0)).end(anyString(), anyString(), anyString(), anyString(), anyInt(), any());
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


        when(register.getRecord("A")).thenReturn(Optional.empty());
        when(register.getRecord("B")).thenReturn(Optional.empty());
        when(register.getRecord("C")).thenReturn(Optional.empty(), Optional.of(new Record(newEntry3, itemS)));
        when(register.getRecord("D")).thenReturn(Optional.empty());

        when(dataAccessLayer.getCurrentIndexEntryNumber("by-x")).thenReturn(0, 1, 2, 3, 4);
        when(dataAccessLayer.getStartIndexEntryNumberAndExistingItemCount("by-x", "Q", "bbb")).thenReturn(new IntegerItemPair(Optional.empty(), 0), new IntegerItemPair(Optional.of(2), 1));
        when(dataAccessLayer.getStartIndexEntryNumberAndExistingItemCount("by-x", "P", "aaa")).thenReturn(new IntegerItemPair(Optional.empty(), 0));
        when(dataAccessLayer.getStartIndexEntryNumberAndExistingItemCount("by-x", "S", "ddd")).thenReturn(new IntegerItemPair(Optional.empty(), 0), new IntegerItemPair(Optional.of(3), 1));
        when(dataAccessLayer.getStartIndexEntryNumberAndExistingItemCount("by-x", "R", "ccc")).thenReturn(new IntegerItemPair(Optional.empty(), 0));

        IndexFunction indexFunction = mock(IndexFunction.class);
        when(indexFunction.getName()).thenReturn("by-x");
        when(indexFunction.execute(register, newEntry1))
                .thenReturn(new HashSet<>(Arrays.asList(new IndexKeyItemPair("P", new HashValue(HashingAlgorithm.SHA256, "aaa")))));
        when(indexFunction.execute(register, newEntry2))
                .thenReturn(new HashSet<>(Arrays.asList(new IndexKeyItemPair("Q", new HashValue(HashingAlgorithm.SHA256, "bbb")))));
        when(indexFunction.execute(register, newEntry3))
                .thenReturn(new HashSet<>(Arrays.asList(new IndexKeyItemPair("S", new HashValue(HashingAlgorithm.SHA256, "ddd")))));
        when(indexFunction.execute(register, newEntry4))
                .thenReturn(new HashSet<>(Arrays.asList(new IndexKeyItemPair("Q", new HashValue(HashingAlgorithm.SHA256, "bbb")))));
        when(indexFunction.execute(register, newEntry5))
                .thenReturn(new HashSet<>(Arrays.asList(new IndexKeyItemPair("R", new HashValue(HashingAlgorithm.SHA256, "ccc")))));

        IndexDriver indexDriver = new IndexDriver(dataAccessLayer);
        indexDriver.indexEntry(register, newEntry1, indexFunction);
        indexDriver.indexEntry(register, newEntry2, indexFunction);
        indexDriver.indexEntry(register, newEntry3, indexFunction);
        indexDriver.indexEntry(register, newEntry4, indexFunction);
        indexDriver.indexEntry(register, newEntry5, indexFunction);

        verify(dataAccessLayer, times(1)).start("by-x", "P", "aaa", 1, 1);
        verify(dataAccessLayer, times(1)).start("by-x", "Q", "bbb", 2, 2);
        verify(dataAccessLayer, times(1)).start("by-x", "S", "ddd", 3, 3);
        verify(dataAccessLayer, times(1)).start("by-x", "Q", "bbb", 4, 2);
        verify(dataAccessLayer, times(1)).end("by-x", "C", "S", "ddd", 4, 4);
        verify(dataAccessLayer, times(1)).start("by-x", "R", "ccc", 5, 5);
    }

    @Test
    public void indexEntry_shouldStartIndexSpecifyingStartIndexEntryNumber_whenItemExistsUnderAnotherIndex() throws IOException {
        Item item = new Item(new HashValue(HashingAlgorithm.SHA256, "aaa"), objectMapper.readTree("{\"x\":\"P\"}"));

        Entry previousEntry = new Entry(1, new HashValue(HashingAlgorithm.SHA256, "aaa"), Instant.now(), "A", EntryType.user);
        Entry newEntry = new Entry(2, new HashValue(HashingAlgorithm.SHA256, "aaa"), Instant.now(), "B", EntryType.user);


        when(register.getRecord(anyString())).thenReturn(Optional.empty());

        when(dataAccessLayer.getCurrentIndexEntryNumber("by-x")).thenReturn(1);
        when(dataAccessLayer.getStartIndexEntryNumberAndExistingItemCount("by-x", "P", "aaa")).thenReturn(new IntegerItemPair(Optional.of(1), 1));
        IndexFunction indexFunction = mock(IndexFunction.class);
        when(indexFunction.getName()).thenReturn("by-x");
        when(indexFunction.execute(register, newEntry))
                .thenReturn(new HashSet<>(Arrays.asList(new IndexKeyItemPair("P", new HashValue(HashingAlgorithm.SHA256, "aaa")))));

        IndexDriver indexDriver = new IndexDriver(dataAccessLayer);
        indexDriver.indexEntry(register, newEntry, indexFunction);

        verify(dataAccessLayer, times(1)).start("by-x", "P", "aaa", 2, 1);
        verify(dataAccessLayer, times(1)).start(anyString(), anyString(), anyString(), anyInt(), any());
        verify(dataAccessLayer, times(0)).end(anyString(), anyString(), anyString(), anyString(), anyInt(), any());
    }

    @Test
    public void indexEntry_shouldEndIndexWithoutSpecifyingEndIndexEntryNumber_whenItemExistsUnderAnotherIndex() throws IOException {
        Item item = new Item(new HashValue(HashingAlgorithm.SHA256, "aaa"), objectMapper.readTree("{\"x\":\"P\"}"));

        Entry previousEntry = new Entry(2, new HashValue(HashingAlgorithm.SHA256, "aaa"), Instant.now(), "B", EntryType.user);
        Entry newEntry = new Entry(3, new HashValue(HashingAlgorithm.SHA256, "aaa"), Instant.now(), "B", EntryType.user);

        when(register.getRecord(anyString())).thenReturn(Optional.of(new Record(previousEntry, item)));

        when(dataAccessLayer.getCurrentIndexEntryNumber("by-x")).thenReturn(1);
        when(dataAccessLayer.getStartIndexEntryNumberAndExistingItemCount("by-x", "P", "aaa")).thenReturn(new IntegerItemPair(Optional.of(2), 2));
        IndexFunction indexFunction = mock(IndexFunction.class);
        when(indexFunction.getName()).thenReturn("by-x");
        when(indexFunction.execute(register, previousEntry))
                .thenReturn(new HashSet<>(Arrays.asList(new IndexKeyItemPair("P", new HashValue(HashingAlgorithm.SHA256, "aaa")))));
        when(indexFunction.execute(register, newEntry))
                .thenReturn(new HashSet<>());

        IndexDriver indexDriver = new IndexDriver(dataAccessLayer);
        indexDriver.indexEntry(register, newEntry, indexFunction);

        verify(dataAccessLayer, times(1)).end("by-x", "B", "P", "aaa", 3, 2);
        verify(dataAccessLayer, times(1)).end(anyString(), anyString(), anyString(), anyString(), anyInt(), any());
        verify(dataAccessLayer, times(0)).start(anyString(), anyString(), anyString(), anyInt(), any());
    }
}
