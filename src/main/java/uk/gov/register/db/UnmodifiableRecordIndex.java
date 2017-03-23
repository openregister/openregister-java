package uk.gov.register.db;

import uk.gov.register.core.Entry;

import javax.inject.Inject;

public class UnmodifiableRecordIndex extends AbstractRecordIndex {
    @Inject
    public UnmodifiableRecordIndex(RecordQueryDAO recordQueryDAO) {
        super(recordQueryDAO);
    }

    @Override
    public void updateRecordIndex(Entry entry) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void checkpoint() {
        // do nothing
    }
}
