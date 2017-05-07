package uk.gov.register.db;

import uk.gov.register.core.Item;
import uk.gov.register.store.DataAccessLayer;

import javax.inject.Inject;

public class UnmodifiableItemStore extends AbstractItemStore {
    @Inject
    public UnmodifiableItemStore(DataAccessLayer dataAccessLayer) {
        super(dataAccessLayer);
    }

    @Override
    public void putItem(Item item) {
        throw new UnsupportedOperationException();
    }

}
