package uk.gov.register.db;

import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.sqlobject.mixins.GetHandle;
import org.skife.jdbi.v2.sqlobject.mixins.Transactional;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;
import uk.gov.register.core.Record;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public abstract class RegisterDAO implements GetHandle, Transactional<RegisterDAO> {

    private final EntryQueryDAO entryQueryDAO;
    private final EntryDAO entryDAO;
    private final ItemQueryDAO itemQueryDAO;
    private final ItemDAO itemDAO;
    private final DestinationDBUpdateDAO destinationDBUpdateDAO;
    private final RecordQueryDAO recordQueryDAO;

    public RegisterDAO() {
        Handle handle = getHandle();
        entryQueryDAO = handle.attach(EntryQueryDAO.class);
        entryDAO = handle.attach(EntryDAO.class);
        itemQueryDAO = handle.attach(ItemQueryDAO.class);
        itemDAO = handle.attach(ItemDAO.class);
        destinationDBUpdateDAO = new DestinationDBUpdateDAO(handle.attach(CurrentKeysUpdateDAO.class));
        recordQueryDAO = handle.attach(RecordQueryDAO.class);
    }

    public Optional<Entry> getEntry(int entryNumber) {
        return entryQueryDAO.findByEntryNumber(entryNumber);
    }

    public Optional<Item> getItemBySha256(String sha256hex) {
        return itemQueryDAO.getItemBySHA256(sha256hex);
    }

    public int getTotalEntries() {
        return entryDAO.currentEntryNumber();
    }

    public Collection<Entry> getEntries(int start, int limit) {
        return entryQueryDAO.getEntries(start, limit);
    }

    public Collection<Entry> getAllEntries() {
        return entryQueryDAO.getAllEntriesNoPagination();
    }

    public Collection<Item> getAllItems() {
        return itemQueryDAO.getAllItemsNoPagination();
    }

    public Optional<Instant> getLastUpdatedTime() {
        return entryQueryDAO.getLastUpdatedTime();
    }

    public void batchInsertItems(Iterable<Item> items) {
        itemDAO.insertInBatch(items);
    }

    public void batchInsertEntries(Iterable<Entry> entries) {
        entryDAO.insertInBatch(entries);
    }

    public void setEntryNumber(int newEntryNumber) {
        entryDAO.setEntryNumber(newEntryNumber);
    }

    public void upsertInCurrentKeysTable(String registerName, List<Record> records) {
        destinationDBUpdateDAO.upsertInCurrentKeysTable(registerName, records);
    }

    public Optional<Record> getRecord(String key) {
        return recordQueryDAO.findByPrimaryKey(key);
    }

    public int getTotalRecords() {
        return recordQueryDAO.getTotalRecords();
    }

    public List<Record> getRecords(int limit, int offset) {
        return recordQueryDAO.getRecords(limit, offset);
    }

    public List<Record> findMax100RecordsByKeyValue(String key, String value) {
        return recordQueryDAO.findMax100RecordsByKeyValue(key, value);
    }

    public Collection<Entry> findAllEntriesOfRecordBy(String registerName, String key) {
        return recordQueryDAO.findAllEntriesOfRecordBy(registerName, key);
    }
}
