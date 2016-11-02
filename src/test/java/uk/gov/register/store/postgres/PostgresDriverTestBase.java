package uk.gov.register.store.postgres;

import org.junit.Before;
import org.mockito.ArgumentCaptor;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.tweak.HandleConsumer;
import uk.gov.register.configuration.RegistersConfiguration;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;
import uk.gov.register.core.Record;
import uk.gov.register.db.*;
import uk.gov.verifiablelog.store.memoization.MemoizationStore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;

public class PostgresDriverTestBase {

    protected List<Entry> entries;
    protected List<Item> items;
    protected List<CurrentKey> currentKeys;

    protected EntryQueryDAO entryQueryDAO;
    protected EntryDAO entryDAO;
    protected ItemQueryDAO itemQueryDAO;
    protected ItemDAO itemDAO;
    protected RecordQueryDAO recordQueryDAO;
    protected CurrentKeysUpdateDAO currentKeysUpdateDAO;

    protected DBI dbi;
    protected Handle handle;
    protected MemoizationStore memoizationStore;

    @Before
    public void setup() {
        entries = new ArrayList<>();
        items = new ArrayList<>();
        currentKeys = new ArrayList<>();

        entryQueryDAO = mock(EntryQueryDAO.class);
        entryDAO = mock(EntryDAO.class);
        itemQueryDAO = mock(ItemQueryDAO.class);
        itemDAO = mock(ItemDAO.class);
        recordQueryDAO = mock(RecordQueryDAO.class);
        currentKeysUpdateDAO = mock(CurrentKeysUpdateDAO.class);

        dbi = mock(DBI.class);
        handle = mock(Handle.class);
        memoizationStore = mock(MemoizationStore.class);

        mockDBI();
        mockEntryDAOInsert();
        mockItemDAOInsert();
        mockCurrentKeysUpdateDAOInsert();
    }

    protected Record mockRecord(String registerName, String key, Integer entryNumber) {
        Entry entry = mock(Entry.class);
        Item item = mock(Item.class);
        when(entry.getEntryNumber()).thenReturn(entryNumber);
        when(item.getKey(registerName)).thenReturn(key);
        return new Record(entry, item);
    }

    private void mockDBI() {
        ArgumentCaptor<HandleConsumer> argumentCaptor = ArgumentCaptor.forClass(HandleConsumer.class);
        doAnswer(invocation -> {
            argumentCaptor.getValue().useHandle(handle);
            return null;
        }).when(dbi).useHandle(argumentCaptor.capture());
    }

    private void mockEntryDAOInsert() {
        ArgumentCaptor<Collection> argumentCaptor = ArgumentCaptor.forClass(Collection.class);
        doAnswer(invocation -> {
            entries.addAll(argumentCaptor.getValue());
            return null;
        }).when(entryDAO).insertInBatch(argumentCaptor.capture());
    }

    private void mockItemDAOInsert() {
        ArgumentCaptor<Collection> argumentCaptor = ArgumentCaptor.forClass(Collection.class);
        doAnswer(invocation -> {
            items.addAll(argumentCaptor.getValue());
            return null;
        }).when(itemDAO).insertInBatch(argumentCaptor.capture());
    }

    private void mockCurrentKeysUpdateDAOInsert() {
        ArgumentCaptor<Collection> argumentCaptor = ArgumentCaptor.forClass(Collection.class);

        doAnswer(invocation -> {
            currentKeys.addAll(argumentCaptor.getValue());
            return null;
        }).when(currentKeysUpdateDAO).writeCurrentKeys(argumentCaptor.capture());

        doAnswer(invocation -> {
            argumentCaptor.getValue().forEach(keyToRemove -> {
                Optional<CurrentKey> currentKeyToRemove = currentKeys.stream().filter(ck -> ck.getKey().equals(keyToRemove)).findFirst();
                if (currentKeyToRemove.isPresent()){
                    currentKeys.remove(currentKeyToRemove.get());
                }
            });
            return null;
        }).when(currentKeysUpdateDAO).removeRecordWithKeys(argumentCaptor.capture());
    }
}
