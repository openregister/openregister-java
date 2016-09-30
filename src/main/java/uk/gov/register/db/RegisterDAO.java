package uk.gov.register.db;

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
    private EntryQueryDAO getEntryQueryDAO() {
        return getHandle().attach(EntryQueryDAO.class);
    }

    private ItemQueryDAO getItemQueryDAO() {
        return getHandle().attach(ItemQueryDAO.class);
    }

    private EntryDAO getEntryDAO() {
        return getHandle().attach(EntryDAO.class);
    }

    private DestinationDBUpdateDAO getDestinationDBUpdateDAO() {
        return new DestinationDBUpdateDAO(getHandle().attach(CurrentKeysUpdateDAO.class));
    }

    private RecordQueryDAO getRecordQueryDAO() {
        return getHandle().attach(RecordQueryDAO.class);
    }

    public Optional<Entry> getEntry(int entryNumber) {
        return getEntryQueryDAO().findByEntryNumber(entryNumber);
    }

    public Optional<Item> getItemBySha256(String sha256hex) {
        return getItemQueryDAO().getItemBySHA256(sha256hex);
    }

    public int getTotalEntries() {
        return getEntryDAO().currentEntryNumber();
    }

    public Collection<Entry> getEntries(int start, int limit) {
        return getEntryQueryDAO().getEntries(start, limit);
    }

    public Collection<Entry> getAllEntries() {
        return getEntryQueryDAO().getAllEntriesNoPagination();
    }

    public Collection<Item> getAllItems() {
        return getItemQueryDAO().getAllItemsNoPagination();
    }

    public Optional<Instant> getLastUpdatedTime() {
        return getEntryQueryDAO().getLastUpdatedTime();
    }

    public void setEntryNumber(int newEntryNumber) {
        getEntryDAO().setEntryNumber(newEntryNumber);
    }

    public void upsertInCurrentKeysTable(String registerName, List<Record> records) {
        getDestinationDBUpdateDAO()
                .upsertInCurrentKeysTable(registerName, records);
    }

    public Optional<Record> getRecord(String key) {
        return getRecordQueryDAO().findByPrimaryKey(key);
    }

    public int getTotalRecords() {
        return getRecordQueryDAO().getTotalRecords();
    }

    public List<Record> getRecords(int limit, int offset) {
        return getRecordQueryDAO().getRecords(limit, offset);
    }

    public List<Record> findMax100RecordsByKeyValue(String key, String value) {
        return getRecordQueryDAO().findMax100RecordsByKeyValue(key, value);
    }

    public Collection<Entry> findAllEntriesOfRecordBy(String registerName, String key) {
        return getRecordQueryDAO().findAllEntriesOfRecordBy(registerName, key);
    }
}
