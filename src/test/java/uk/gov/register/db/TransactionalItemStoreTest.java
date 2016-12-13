package uk.gov.register.db;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;
import uk.gov.register.exceptions.ItemValidationException;
import uk.gov.register.service.ItemValidator;
import uk.gov.register.util.HashValue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static uk.gov.register.core.HashingAlgorithm.SHA256;

public class TransactionalItemStoreTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Item item1;
    private Item item2;
    private final ArrayList<Entry> entries = newArrayList();
    InMemoryItemDAO itemDAO;
    Map<HashValue, Item> itemMap;
    private TransactionalItemStore itemStore;
    private ItemValidator itemValidator;

    @Before
    public void setUp() throws Exception {
        itemMap = new HashMap<>();
        itemDAO = new InMemoryItemDAO(itemMap, new InMemoryEntryDAO(entries));
        itemValidator = mock(ItemValidator.class);
        itemStore = new TransactionalItemStore(itemDAO, itemDAO, itemValidator);
        item1 = new Item(new HashValue(SHA256, "abcd"), objectMapper.readTree("{}"));
        item2 = new Item(new HashValue(SHA256, "jkl1"), objectMapper.readTree("{}"));
    }

    @Test
    public void putItem_shouldNotCommitData() {
        itemStore.putItem(item1);
        itemStore.putItem(item2);

        assertThat(itemMap.entrySet(), is(empty()));
    }

    @Test(expected = ItemValidationException.class)
    public void putItem_shouldRejectInvalidItems() {
        doThrow(new ItemValidationException("foo", null))
                .when(itemValidator).validateItem(any(JsonNode.class));
        itemStore.putItem(item1);
    }

    @Test
    public void getAllItems_shouldGetFromStagedDataIfNeeded() throws Exception {
        itemStore.putItem(item1);
        itemStore.putItem(item2);

        assertThat(itemStore.getAllItems(), is(iterableWithSize(2)));
    }

    @Test
    public void getItemBySha256_shouldGetFromStagedDataIfNeeded() throws Exception {
        itemStore.putItem(item1);
        itemStore.putItem(item2);

        assertThat(itemStore.getItemBySha256(item1.getSha256hex()), is(Optional.of(item1)));
    }

    @Test
    public void getIterator_shouldGetFromStagedDataIfNeeded() throws Exception {
        itemStore.putItem(item1);
        itemStore.putItem(item2);
        entries.add(new Entry(1, item1.getSha256hex(), Instant.ofEpochSecond(12345), "12345"));
        entries.add(new Entry(2, item2.getSha256hex(), Instant.ofEpochSecond(54321), "54321"));

        List<Item> items = newArrayList(itemStore.getIterator());
        assertThat(items, is(asList(item1, item2)));
    }

    @Test
    public void getIteratorRange_shouldGetFromStagedDataIfNeeded() throws Exception {
        itemStore.putItem(item1);
        itemStore.putItem(item2);
        entries.add(new Entry(1, item1.getSha256hex(), Instant.ofEpochSecond(12345), "12345"));
        entries.add(new Entry(2, item2.getSha256hex(), Instant.ofEpochSecond(54321), "54321"));

        List<Item> items = newArrayList(itemStore.getIterator(1,2));
        assertThat(items, is(singletonList(item2)));
    }
}
