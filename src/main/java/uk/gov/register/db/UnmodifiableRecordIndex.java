package uk.gov.register.db;

import javax.inject.Inject;

public class UnmodifiableRecordIndex extends AbstractRecordIndex {
    @Inject
    public UnmodifiableRecordIndex(RecordQueryDAO recordQueryDAO) {
        super(recordQueryDAO);
    }

    @Override
    public void updateRecordIndex(String key, Integer entryNumber) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeRecordIndex(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void checkpoint() {
        // do nothing
    }
}
