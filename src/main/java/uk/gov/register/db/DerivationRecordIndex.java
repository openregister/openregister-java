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
        if ("true".equals(System.getProperty("multi-item-entries-enabled"))) {
            return indexQueryDAO.findRecord(key, derivationName);
        } else {
            return Optional.empty();
        }
    }

    public List<Record> getRecords(int limit, int offset, String derivationName) {
        if ("true".equals(System.getProperty("multi-item-entries-enabled"))) {
            return indexQueryDAO.findRecords(limit, offset, derivationName);
        } else {
            return Collections.emptyList();
        }
    }
}
