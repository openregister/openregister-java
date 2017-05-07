package uk.gov.register.db;

import uk.gov.register.core.Entry;
import uk.gov.register.store.DataAccessLayer;

public class TransactionalRecordIndex extends AbstractRecordIndex {
    private final DataAccessLayer dataAccessLayer;

    public TransactionalRecordIndex(DataAccessLayer dataAccessLayer) {
        super(dataAccessLayer);
        this.dataAccessLayer = dataAccessLayer;
    }

    @Override
    public void updateRecordIndex(Entry entry) {
        dataAccessLayer.updateRecordIndex(entry);
    }
}
