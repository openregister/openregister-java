package uk.gov.register.db;

import uk.gov.register.core.Entry;
import uk.gov.register.store.DataAccessLayer;

import javax.inject.Inject;

public class UnmodifiableRecordIndex extends AbstractRecordIndex {
    @Inject
    public UnmodifiableRecordIndex(DataAccessLayer dataAccessLayer) {
        super(dataAccessLayer);
    }

    @Override
    public void updateRecordIndex(Entry entry) {
        throw new UnsupportedOperationException();
    }
}
