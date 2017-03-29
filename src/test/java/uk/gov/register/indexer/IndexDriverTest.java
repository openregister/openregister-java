package uk.gov.register.indexer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.register.core.*;
import uk.gov.register.db.IndexDAO;
import uk.gov.register.db.IndexQueryDAO;
import uk.gov.register.indexer.function.IndexFunction;
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

    @Test
    public void getStartIndices_shouldReturnEmptyList_whenNoNewPairsExist() {
        Register register = mock(Register.class);
        IndexDAO indexDAO = mock(IndexDAO.class);
        IndexQueryDAO indexQueryDAO = mock(IndexQueryDAO.class);
        IndexDriver indexDriver = new IndexDriver(register, indexDAO, indexQueryDAO);

        Set<IndexValueItemPair> existingPairs = new HashSet<>(Arrays.asList(
                new IndexValueItemPair("A", new HashValue(HashingAlgorithm.SHA256, "aaa")),
                new IndexValueItemPair("B", new HashValue(HashingAlgorithm.SHA256, "bbb"))
        ));
        Set<IndexValueItemPair> newPairs = new HashSet<>(Arrays.asList(
                new IndexValueItemPair("A", new HashValue(HashingAlgorithm.SHA256, "aaa")),
                new IndexValueItemPair("B", new HashValue(HashingAlgorithm.SHA256, "bbb"))
        ));

        List<IndexValueItemPairEvent> pairsToStart = indexDriver.getStartIndices(existingPairs, newPairs);

        assertThat(pairsToStart, empty());
    }

    @Test
    public void getStartIndices_shouldReturnNewPairs_whenNewPairsExist() {
        Register register = mock(Register.class);
        IndexDAO indexDAO = mock(IndexDAO.class);
        IndexQueryDAO indexQueryDAO = mock(IndexQueryDAO.class);
        IndexDriver indexDriver = new IndexDriver(register, indexDAO, indexQueryDAO);

        Set<IndexValueItemPair> existingPairs = new HashSet<>(Arrays.asList(
                new IndexValueItemPair("A", new HashValue(HashingAlgorithm.SHA256, "aaa")),
                new IndexValueItemPair("B", new HashValue(HashingAlgorithm.SHA256, "bbb"))
        ));
        Set<IndexValueItemPair> newPairs = new HashSet<>(Arrays.asList(
                new IndexValueItemPair("A", new HashValue(HashingAlgorithm.SHA256, "aaa")),
                new IndexValueItemPair("B", new HashValue(HashingAlgorithm.SHA256, "bbb")),
                new IndexValueItemPair("C", new HashValue(HashingAlgorithm.SHA256, "ccc"))
        ));

        List<IndexValueItemPairEvent> pairsToStart = indexDriver.getStartIndices(existingPairs, newPairs);

        assertThat(pairsToStart, contains(new IndexValueItemPairEvent(new IndexValueItemPair("C", new HashValue(HashingAlgorithm.SHA256, "ccc")), true)));
    }

    @Test
    public void getEndIndices_shouldReturnEmptyList_whenNoNewPairsExist() {
        Register register = mock(Register.class);
        IndexDAO indexDAO = mock(IndexDAO.class);
        IndexQueryDAO indexQueryDAO = mock(IndexQueryDAO.class);
        IndexDriver indexDriver = new IndexDriver(register, indexDAO, indexQueryDAO);

        Set<IndexValueItemPair> existingPairs = new HashSet<>(Arrays.asList(
                new IndexValueItemPair("A", new HashValue(HashingAlgorithm.SHA256, "aaa")),
                new IndexValueItemPair("B", new HashValue(HashingAlgorithm.SHA256, "bbb"))
        ));
        Set<IndexValueItemPair> newPairs = new HashSet<>(Arrays.asList(
                new IndexValueItemPair("B", new HashValue(HashingAlgorithm.SHA256, "bbb")),
                new IndexValueItemPair("A", new HashValue(HashingAlgorithm.SHA256, "aaa"))
        ));

        List<IndexValueItemPairEvent> pairsToEnd = indexDriver.getEndIndices(existingPairs, newPairs);

        assertThat(pairsToEnd, empty());
    }

    @Test
    public void getEndIndices_shouldReturnEndedPairs_whenEndedPairsExist() {
        Register register = mock(Register.class);
        IndexDAO indexDAO = mock(IndexDAO.class);
        IndexQueryDAO indexQueryDAO = mock(IndexQueryDAO.class);
        IndexDriver indexDriver = new IndexDriver(register, indexDAO, indexQueryDAO);

        Set<IndexValueItemPair> existingPairs = new HashSet<>(Arrays.asList(
                new IndexValueItemPair("A", new HashValue(HashingAlgorithm.SHA256, "aaa")),
                new IndexValueItemPair("B", new HashValue(HashingAlgorithm.SHA256, "bbb"))
        ));
        Set<IndexValueItemPair> newPairs = new HashSet<>(Arrays.asList(
                new IndexValueItemPair("B", new HashValue(HashingAlgorithm.SHA256, "bbb"))
        ));

        List<IndexValueItemPairEvent> pairsToEnd = indexDriver.getEndIndices(existingPairs, newPairs);

        assertThat(pairsToEnd, contains(new IndexValueItemPairEvent(new IndexValueItemPair("A", new HashValue(HashingAlgorithm.SHA256, "aaa")), false)));
    }

    @Test
    public void indexEntry_shouldStartIndex_whenNewPairsExist() throws IOException {
        Item item = new Item(new HashValue(HashingAlgorithm.SHA256, "aaa"), objectMapper.readTree("{\"x\":\"P\"}"));

        Entry previousEntry = new Entry(1, new HashValue(HashingAlgorithm.SHA256, "aaa"), Instant.now(), "A");
        Entry newEntry = new Entry(2, Arrays.asList(new HashValue(HashingAlgorithm.SHA256, "aaa"), new HashValue(HashingAlgorithm.SHA256, "bbb")), Instant.now(), "A");

        Register register = mock(Register.class);
        when(register.getRecord("A")).thenReturn(Optional.of(new Record(previousEntry, item)));

        IndexDAO indexDAO = mock(IndexDAO.class);
        IndexQueryDAO indexQueryDAO = mock(IndexQueryDAO.class);
        when(indexQueryDAO.getCurrentIndexEntryNumber("by-x")).thenReturn(0);
        IndexFunction indexFunction = mock(IndexFunction.class);
        when(indexFunction.getName()).thenReturn("by-x");
        when(indexFunction.execute(previousEntry))
                .thenReturn(new HashSet<>(Arrays.asList(new IndexValueItemPair("P", new HashValue(HashingAlgorithm.SHA256, "aaa")))));
        when(indexFunction.execute(newEntry))
                .thenReturn(new HashSet<>(Arrays.asList(new IndexValueItemPair("P", new HashValue(HashingAlgorithm.SHA256, "aaa")), new IndexValueItemPair("P", new HashValue(HashingAlgorithm.SHA256, "bbb")))));

        IndexDriver indexDriver = new IndexDriver(register, indexDAO, indexQueryDAO);
        indexDriver.indexEntry(newEntry, indexFunction);

        verify(indexDAO, times(1)).start("by-x", "P", "bbb", 2, 1);
        verifyNoMoreInteractions(indexDAO);
    }

    @Test
    public void indexEntry_shouldEndIndex_whenEndedPairExists() throws IOException {
        Item item = new Item(new HashValue(HashingAlgorithm.SHA256, "aaa"), objectMapper.readTree("{\"x\":\"P\"}"));

        Entry previousEntry = new Entry(1, Arrays.asList(new HashValue(HashingAlgorithm.SHA256, "aaa"), new HashValue(HashingAlgorithm.SHA256, "bbb")), Instant.now(), "A");
        Entry newEntry = new Entry(2, new HashValue(HashingAlgorithm.SHA256, "bbb"), Instant.now(), "A");

        Register register = mock(Register.class);
        when(register.getRecord("A")).thenReturn(Optional.of(new Record(previousEntry, item)));

        IndexDAO indexDAO = mock(IndexDAO.class);
        IndexQueryDAO indexQueryDAO = mock(IndexQueryDAO.class);
        when(indexQueryDAO.getCurrentIndexEntryNumber("by-x")).thenReturn(0);
        IndexFunction indexFunction = mock(IndexFunction.class);
        when(indexFunction.getName()).thenReturn("by-x");
        when(indexFunction.execute(previousEntry))
                .thenReturn(new HashSet<>(Arrays.asList(new IndexValueItemPair("P", new HashValue(HashingAlgorithm.SHA256, "aaa")), new IndexValueItemPair("P", new HashValue(HashingAlgorithm.SHA256, "bbb")))));
        when(indexFunction.execute(newEntry))
                .thenReturn(new HashSet<>(Arrays.asList(new IndexValueItemPair("P", new HashValue(HashingAlgorithm.SHA256, "bbb")))));

        IndexDriver indexDriver = new IndexDriver(register, indexDAO, indexQueryDAO);
        indexDriver.indexEntry(newEntry, indexFunction);

        verify(indexDAO, times(1)).end("by-x", "P", "aaa", 2, 1);
        verifyNoMoreInteractions(indexDAO);
    }

    @Test
    public void indexEntry_shouldEndThenStartIndex_usingAlphabeticalKeyOrder_whenItemMovesFromOneGroupToAnother() throws IOException {
        Item itemP = new Item(new HashValue(HashingAlgorithm.SHA256, "aaa"), objectMapper.readTree("{\"x\":\"P\"}"));
        Item itemQ = new Item(new HashValue(HashingAlgorithm.SHA256, "bbb"), objectMapper.readTree("{\"x\":\"Q\"}"));

        Entry previousEntry = new Entry(1, new HashValue(HashingAlgorithm.SHA256, "aaa"), Instant.now(), "A");
        Entry newEntry = new Entry(2, new HashValue(HashingAlgorithm.SHA256, "bbb"), Instant.now(), "A");

        Register register = mock(Register.class);
        when(register.getRecord("A")).thenReturn(Optional.of(new Record(previousEntry, itemP)));

        IndexDAO indexDAO = mock(IndexDAO.class);
        IndexQueryDAO indexQueryDAO = mock(IndexQueryDAO.class);
        when(indexQueryDAO.getCurrentIndexEntryNumber("by-x")).thenReturn(0);
        IndexFunction indexFunction = mock(IndexFunction.class);
        when(indexFunction.getName()).thenReturn("by-x");
        when(indexFunction.execute(previousEntry))
                .thenReturn(new HashSet<>(Arrays.asList(new IndexValueItemPair("P", new HashValue(HashingAlgorithm.SHA256, "aaa")))));
        when(indexFunction.execute(newEntry))
                .thenReturn(new HashSet<>(Arrays.asList(new IndexValueItemPair("Q", new HashValue(HashingAlgorithm.SHA256, "bbb")))));


        IndexDriver indexDriver = new IndexDriver(register, indexDAO, indexQueryDAO);
        indexDriver.indexEntry(newEntry, indexFunction);

        verify(indexDAO, times(1)).end("by-x", "P", "aaa", 2, 1);
        verify(indexDAO, times(1)).start("by-x", "Q", "bbb", 2, 2);
        verifyNoMoreInteractions(indexDAO);
    }

    @Test
    public void indexEntry_shouldStartThenEndIndex_usingAlphabeticalKeyOrder_whenItemMovesFromOneGroupToAnother() throws IOException {
        Item itemQ = new Item(new HashValue(HashingAlgorithm.SHA256, "bbb"), objectMapper.readTree("{\"x\":\"Q\"}"));
        Item itemP = new Item(new HashValue(HashingAlgorithm.SHA256, "aaa"), objectMapper.readTree("{\"x\":\"P\"}"));

        Entry previousEntry = new Entry(1, new HashValue(HashingAlgorithm.SHA256, "bbb"), Instant.now(), "A");
        Entry newEntry = new Entry(2, new HashValue(HashingAlgorithm.SHA256, "aaa"), Instant.now(), "A");

        Register register = mock(Register.class);
        when(register.getRecord("A")).thenReturn(Optional.of(new Record(previousEntry, itemQ)));

        IndexDAO indexDAO = mock(IndexDAO.class);
        IndexQueryDAO indexQueryDAO = mock(IndexQueryDAO.class);
        when(indexQueryDAO.getCurrentIndexEntryNumber("by-x")).thenReturn(0);
        IndexFunction indexFunction = mock(IndexFunction.class);
        when(indexFunction.getName()).thenReturn("by-x");
        when(indexFunction.execute(previousEntry))
                .thenReturn(new HashSet<>(Arrays.asList(new IndexValueItemPair("Q", new HashValue(HashingAlgorithm.SHA256, "bbb")))));
        when(indexFunction.execute(newEntry))
                .thenReturn(new HashSet<>(Arrays.asList(new IndexValueItemPair("P", new HashValue(HashingAlgorithm.SHA256, "aaa")))));


        IndexDriver indexDriver = new IndexDriver(register, indexDAO, indexQueryDAO);
        indexDriver.indexEntry(newEntry, indexFunction);

        verify(indexDAO, times(1)).start("by-x", "P", "aaa", 2, 1);
        verify(indexDAO, times(1)).end("by-x", "Q", "bbb", 2, 2);
        verifyNoMoreInteractions(indexDAO);
    }

    @Test
    public void indexEntry_shouldEndAndStartIndex_whenItemChangesWithoutMovingGroup() throws IOException {
        Item itemP = new Item(new HashValue(HashingAlgorithm.SHA256, "aaa"), objectMapper.readTree("{\"x\":\"P\",\"y\":\"S\"}"));
        Item itemQ = new Item(new HashValue(HashingAlgorithm.SHA256, "bbb"), objectMapper.readTree("{\"x\":\"P\",\"y\":\"T\"}"));

        Entry previousEntry = new Entry(1, new HashValue(HashingAlgorithm.SHA256, "aaa"), Instant.now(), "A");
        Entry newEntry = new Entry(2, new HashValue(HashingAlgorithm.SHA256, "bbb"), Instant.now(), "A");

        Register register = mock(Register.class);
        when(register.getRecord("A")).thenReturn(Optional.of(new Record(previousEntry, itemP)));

        IndexDAO indexDAO = mock(IndexDAO.class);
        IndexQueryDAO indexQueryDAO = mock(IndexQueryDAO.class);
        when(indexQueryDAO.getCurrentIndexEntryNumber("by-x")).thenReturn(0);
        IndexFunction indexFunction = mock(IndexFunction.class);
        when(indexFunction.getName()).thenReturn("by-x");
        when(indexFunction.execute(previousEntry))
                .thenReturn(new HashSet<>(Arrays.asList(new IndexValueItemPair("P", new HashValue(HashingAlgorithm.SHA256, "aaa")))));
        when(indexFunction.execute(newEntry))
                .thenReturn(new HashSet<>(Arrays.asList(new IndexValueItemPair("P", new HashValue(HashingAlgorithm.SHA256, "bbb")))));


        IndexDriver indexDriver = new IndexDriver(register, indexDAO, indexQueryDAO);
        indexDriver.indexEntry(newEntry, indexFunction);

        verify(indexDAO, times(1)).end("by-x", "P", "aaa", 2, 1);
        verify(indexDAO, times(1)).start("by-x", "P", "bbb", 2, 1);
        verifyNoMoreInteractions(indexDAO);
    }

    @Test
    public void indexEntry_shouldStartIndexesWithIncreasingIndexNumbers_whenAddingItems() throws IOException {
        Item itemP = new Item(new HashValue(HashingAlgorithm.SHA256, "aaa"), objectMapper.readTree("{\"x\":\"P\"}"));
        Item itemQ = new Item(new HashValue(HashingAlgorithm.SHA256, "bbb"), objectMapper.readTree("{\"x\":\"Q\"}"));

        Entry newEntry1 = new Entry(1, new HashValue(HashingAlgorithm.SHA256, "aaa"), Instant.now(), "A");
        Entry newEntry2 = new Entry(2, new HashValue(HashingAlgorithm.SHA256, "bbb"), Instant.now(), "B");

        Register register = mock(Register.class);
        when(register.getRecord(anyString())).thenReturn(Optional.empty());

        IndexDAO indexDAO = mock(IndexDAO.class);
        IndexQueryDAO indexQueryDAO = mock(IndexQueryDAO.class);
        when(indexQueryDAO.getCurrentIndexEntryNumber("by-x")).thenReturn(0, 1);
        IndexFunction indexFunction = mock(IndexFunction.class);
        when(indexFunction.getName()).thenReturn("by-x");
        when(indexFunction.execute(newEntry1))
                .thenReturn(new HashSet<>(Arrays.asList(new IndexValueItemPair("P", new HashValue(HashingAlgorithm.SHA256, "aaa")))));
        when(indexFunction.execute(newEntry2))
                .thenReturn(new HashSet<>(Arrays.asList(new IndexValueItemPair("Q", new HashValue(HashingAlgorithm.SHA256, "bbb")))));

        IndexDriver indexDriver = new IndexDriver(register, indexDAO, indexQueryDAO);
        indexDriver.indexEntry(newEntry1, indexFunction);
        indexDriver.indexEntry(newEntry2, indexFunction);

        verify(indexDAO, times(1)).start("by-x", "P", "aaa", 1, 1);
        verify(indexDAO, times(1)).start("by-x", "Q", "bbb", 2, 2);
        verifyNoMoreInteractions(indexDAO);
    }
}
