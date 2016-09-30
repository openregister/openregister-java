package uk.gov.register.db;

import org.skife.jdbi.v2.sqlobject.mixins.GetHandle;
import org.skife.jdbi.v2.sqlobject.mixins.Transactional;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Record;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public abstract class RegisterDAO implements GetHandle, Transactional<RegisterDAO> {

    private DestinationDBUpdateDAO getDestinationDBUpdateDAO() {
        return new DestinationDBUpdateDAO(getHandle().attach(CurrentKeysUpdateDAO.class));
    }

    private RecordQueryDAO getRecordQueryDAO() {
        return getHandle().attach(RecordQueryDAO.class);
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
