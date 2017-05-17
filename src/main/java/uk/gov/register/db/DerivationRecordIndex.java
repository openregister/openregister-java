package uk.gov.register.db;

import uk.gov.register.core.Record;
import uk.gov.register.store.DataAccessLayer;

import java.util.List;
import java.util.Optional;

public class DerivationRecordIndex {

    private final DataAccessLayer dataAccessLayer;

    public DerivationRecordIndex(DataAccessLayer dataAccessLayer) {
        this.dataAccessLayer = dataAccessLayer;
    }

    public Optional<Record> getRecord(String key, String derivationName) {
        Optional<Record> record = dataAccessLayer.getIndexRecord(key, derivationName);
        return record.filter(r -> r.getItems().size() != 0);
    }

    public List<Record> getRecords(int limit, int offset, String derivationName) {
        return dataAccessLayer.getIndexRecords(limit, offset, derivationName);
    }

    public int getTotalRecords(String derivationName) {
        return dataAccessLayer.getTotalIndexRecords(derivationName);
    }
}
