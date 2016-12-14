package uk.gov.register.db;

import uk.gov.register.core.Entry;
import uk.gov.register.core.Record;
import uk.gov.register.core.RecordIndex;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public abstract class AbstractRecordIndex implements RecordIndex {
    protected final RecordQueryDAO recordQueryDAO;

    public AbstractRecordIndex(RecordQueryDAO recordQueryDAO) {
        this.recordQueryDAO = recordQueryDAO;
    }

    @Override
    public Optional<Record> getRecord(String key) {
        checkpoint();
        return recordQueryDAO.findByPrimaryKey(key);
    }

    @Override
    public int getTotalRecords() {
        checkpoint();
        return recordQueryDAO.getTotalRecords();
    }

    @Override
    public List<Record> getRecords(int limit, int offset) {
        checkpoint();
        return recordQueryDAO.getRecords(limit, offset);
    }

    @Override
    public List<Record> findMax100RecordsByKeyValue(String key, String value) {
        checkpoint();
        return recordQueryDAO.findMax100RecordsByKeyValue(key, value);
    }

    @Override
    public Collection<Entry> findAllEntriesOfRecordBy(String registerName, String key) {
        checkpoint();
        return recordQueryDAO.findAllEntriesOfRecordBy(registerName, key);
    }
}
