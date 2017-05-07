package uk.gov.register.db;

import uk.gov.register.core.Item;
import uk.gov.register.service.ItemValidator;
import uk.gov.register.store.DataAccessLayer;

public class TransactionalItemStore extends AbstractItemStore {
    private final DataAccessLayer dataAccessLayer;
    private final ItemValidator itemValidator;

    public TransactionalItemStore(DataAccessLayer dataAccessLayer, ItemValidator itemValidator) {
        super(dataAccessLayer);
        this.dataAccessLayer = dataAccessLayer;
        this.itemValidator = itemValidator;
    }

    @Override
    public void putItem(Item item) {
        itemValidator.validateItem(item.getContent());
        dataAccessLayer.putItem(item);
    }
}
