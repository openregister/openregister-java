package uk.gov.register.db;

import uk.gov.register.core.Item;

import javax.inject.Inject;

public class UnmodifiableItemStore extends AbstractItemStore {
    @Inject
    public UnmodifiableItemStore(ItemQueryDAO itemQueryDAO) {
        super(itemQueryDAO);
    }

    @Override
    public void putItem(Item item) {
        throw new UnsupportedOperationException();
    }

}
