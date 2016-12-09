package uk.gov.register.store.postgres;

import org.junit.Before;
import org.mockito.ArgumentCaptor;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.tweak.HandleConsumer;
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

public class PostgresDriverTestBase {

    protected List<Item> items;
    protected List<CurrentKey> currentKeys;

    protected RecordQueryDAO recordQueryDAO;
    protected CurrentKeysUpdateDAO currentKeysUpdateDAO;

    protected DBI dbi;
    protected Handle handle;
    protected MemoizationStore memoizationStore;

    @Before
    public void setup() {
        items = new ArrayList<>();
        currentKeys = new ArrayList<>();

        recordQueryDAO = mock(RecordQueryDAO.class);
        currentKeysUpdateDAO = mock(CurrentKeysUpdateDAO.class);

        dbi = mock(DBI.class);
        handle = mock(Handle.class);
        memoizationStore = mock(MemoizationStore.class);

        mockDBI();
        mockCurrentKeysUpdateDAOInsert();
    }

    protected Record mockRecord(String registerName, String key, Integer entryNumber) {
        Entry entry = mock(Entry.class);
        Item item = mock(Item.class);
        when(entry.getEntryNumber()).thenReturn(entryNumber);
        when(item.getValue(registerName)).thenReturn(key);
        return new Record(entry, item);
    }

    private void mockDBI() {
        ArgumentCaptor<HandleConsumer> argumentCaptor = ArgumentCaptor.forClass(HandleConsumer.class);
        doAnswer(invocation -> {
            argumentCaptor.getValue().useHandle(handle);
            return null;
        }).when(dbi).useHandle(argumentCaptor.capture());
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
            return new int[]{1};
        }).when(currentKeysUpdateDAO).removeRecordWithKeys(argumentCaptor.capture());
    }
}
