package uk.gov.indexer.dao;

import com.google.common.collect.Iterables;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.TransactionIsolationLevel;
import org.skife.jdbi.v2.sqlobject.Transaction;
import org.skife.jdbi.v2.sqlobject.mixins.GetHandle;

import java.util.List;

public abstract class DestinationDBUpdateDAO_NewSchema implements GetHandle, DBConnectionDAO {
    private final EntryUpdateDAO entryUpdateDAO;
    private final ItemUpdateDAO itemUpdateDAO;

    public DestinationDBUpdateDAO_NewSchema() {
        Handle handle = getHandle();
        entryUpdateDAO = handle.attach(EntryUpdateDAO.class);
        entryUpdateDAO.ensureEntryTableInPlace();

        itemUpdateDAO = handle.attach(ItemUpdateDAO.class);
        itemUpdateDAO.ensureItemTableInPlace();
    }

    public int lastReadEntryNumber() {
        return entryUpdateDAO.lastReadEntryNumber();
    }

    @Transaction(TransactionIsolationLevel.SERIALIZABLE)
    public void writeEntriesAndItemsInBatch(List<Record> records) {
        entryUpdateDAO.writeBatch(Iterables.transform(records, record -> record.entry));
        itemUpdateDAO.writeBatch(Iterables.transform(records, record -> record.item));
    }
}
