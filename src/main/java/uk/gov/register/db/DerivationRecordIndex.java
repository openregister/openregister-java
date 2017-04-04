package uk.gov.register.db;

import uk.gov.register.core.Record;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class DerivationRecordIndex {

    private final IndexQueryDAO indexQueryDAO;

    public DerivationRecordIndex(IndexQueryDAO indexQueryDAO) {
        this.indexQueryDAO = indexQueryDAO;
    }

    public Optional<Record> getRecord(String key, String derivationName) {
        Optional<Record> record = indexQueryDAO.findRecord(key, derivationName);
        return record.filter(r -> r.getItems().size() != 0);
    }

    public List<Record> getRecords(int limit, int offset, String derivationName) {
        return indexQueryDAO.findRecords(limit, offset, derivationName);
    }
}
