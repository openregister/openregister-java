package uk.gov.register.store.postgres;

import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.TransactionCallback;
import org.skife.jdbi.v2.tweak.HandleCallback;
import org.skife.jdbi.v2.tweak.HandleConsumer;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;
import uk.gov.register.core.Record;
import uk.gov.register.db.CurrentKey;
import uk.gov.verifiablelog.store.memoization.MemoizationStore;

import javax.inject.Inject;
import java.util.Arrays;

public class PostgresDriverNonTransactional extends PostgresDriver {

    private DBI dbi;

    @Inject
    public PostgresDriverNonTransactional(DBI dbi, MemoizationStore memoizationStore) {
        super(memoizationStore);
        this.dbi = dbi;
    }

    @Override
    public void insertEntry(Entry entry) {
        super.insertEntries(Arrays.asList(entry));
    }

    @Override
    public void insertItem(Item item) {
        super.insertItems(Arrays.asList(item));
    }

    @Override
    public void insertRecord(Record record, String registerName) {
        super.insertCurrentKeys(Arrays.asList(new CurrentKey(record.item.getKey(registerName), record.entry.getEntryNumber())));
    }

    @Override
    protected void useHandle(HandleConsumer callback) {
        dbi.useHandle(callback);
    }

    @Override
    protected <ReturnType> ReturnType withHandle(HandleCallback<ReturnType> callback) {
        return dbi.withHandle(callback);
    }

    @Override
    protected <ReturnType> ReturnType inTransaction(TransactionCallback<ReturnType> callback) {
        return dbi.inTransaction(callback);
    }
}
