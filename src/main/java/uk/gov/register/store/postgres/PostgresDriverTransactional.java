package uk.gov.register.store.postgres;

import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.TransactionIsolationLevel;
import org.skife.jdbi.v2.exceptions.CallbackFailedException;
import org.skife.jdbi.v2.tweak.HandleCallback;
import org.skife.jdbi.v2.tweak.HandleConsumer;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;
import uk.gov.register.core.Record;
import uk.gov.register.db.CurrentKey;
import uk.gov.verifiablelog.store.memoization.MemoizationStore;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PostgresDriverTransactional extends PostgresDriver {
    private final Handle handle;

    private final List<Entry> stagedEntries;
    private final Set<Item> stagedItems;
    private final HashMap<String, Integer> stagedCurrentKeys;

    private PostgresDriverTransactional(Handle handle, MemoizationStore memoizationStore) {
        super(memoizationStore);

        this.handle = handle;
        this.stagedEntries = new ArrayList<>();
        this.stagedItems = new HashSet<>();
        this.stagedCurrentKeys = new HashMap<>();
    }

    public static void useTransaction(DBI dbi, MemoizationStore memoizationStore, Consumer<PostgresDriverTransactional> callback) {
        dbi.useTransaction(TransactionIsolationLevel.SERIALIZABLE, (handle, status) -> {
            callback.accept(new PostgresDriverTransactional(handle, memoizationStore));
            callback.andThen(pd -> pd.writeStagedData());
        });
    }

    @Override
    public void insertEntry(Entry entry) {
        stagedEntries.add(entry);
    }

    @Override
    public void insertItem(Item item) {
        stagedItems.add(item);
    }

    @Override
    public void insertRecord(Record record, String registerName) {
        stagedCurrentKeys.put(record.item.getKey(registerName), record.entry.getEntryNumber());
    }

    @Override
    protected void useHandle(HandleConsumer callback) {
        useExistingHandle(callback);
    }

    @Override
    protected <ReturnType> ReturnType withHandle(HandleCallback<ReturnType> callback) {
        return useExistingHandle(callback);
    }

    @Override
    protected <ReturnType> ReturnType inTransaction(HandleCallback<ReturnType> callback) {
        return useExistingHandle(callback);
    }

    private void useExistingHandle(HandleConsumer callback) {
        try {
            writeStagedData();
            callback.useHandle(handle);
        } catch (Exception ex) {
            throw new CallbackFailedException(ex);
        }
    }

    private <ReturnType> ReturnType useExistingHandle(HandleCallback<ReturnType> callback) {
        try {
            writeStagedData();
            return callback.withHandle(handle);
        } catch (Exception ex) {
            throw new CallbackFailedException(ex);
        }
    }

    private void writeStagedData() {
        writeEntries();
        writeItems();
        writeCurrentKeys();
    }

    private void writeEntries() {
        if (stagedEntries.isEmpty()) {
            return;
        }
        super.insertEntries(stagedEntries);
        stagedEntries.clear();
    }

    private void writeItems() {
        if (stagedItems.isEmpty()) {
            return;
        }
        super.insertItems(stagedItems);
        stagedItems.clear();
    }

    private void writeCurrentKeys() {
        if (stagedCurrentKeys.isEmpty()) {
            return;
        }

        List<CurrentKey> currentKeysToWrite = stagedCurrentKeys.entrySet().stream()
                .map(ck -> new CurrentKey(ck.getKey(), ck.getValue()))
                .collect(Collectors.toList());

        super.insertCurrentKeys(currentKeysToWrite);
        stagedCurrentKeys.clear();
    }
}
