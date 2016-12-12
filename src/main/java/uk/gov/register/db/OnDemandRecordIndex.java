package uk.gov.register.db;

import uk.gov.register.core.Entry;
import uk.gov.register.core.Record;
import uk.gov.register.core.RecordIndex;

import javax.inject.Inject;
import java.util.*;

public class OnDemandRecordIndex implements RecordIndex {
    private final RecordQueryDAO recordQueryDAO;

    @Inject
    public OnDemandRecordIndex(RecordQueryDAO recordQueryDAO) {
        this.recordQueryDAO = recordQueryDAO;
    }

    @Override
    public void updateRecordIndex(String key, Integer entryNumber) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<Record> getRecord(String key) {
        return recordQueryDAO.findByPrimaryKey(key);
    }

    @Override
    public int getTotalRecords() {
        return recordQueryDAO.getTotalRecords();
    }

    @Override
    public List<Record> getRecords(int limit, int offset) {
        return recordQueryDAO.getRecords(limit, offset);
    }

    @Override
    public List<Record> findMax100RecordsByKeyValue(String key, String value) {
        return recordQueryDAO.findMax100RecordsByKeyValue(key, value);
    }

    @Override
    public Collection<Entry> findAllEntriesOfRecordBy(String registerName, String key) {
        return recordQueryDAO.findAllEntriesOfRecordBy(registerName, key);
    }
}
