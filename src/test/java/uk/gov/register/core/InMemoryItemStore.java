package uk.gov.register.core;

import uk.gov.register.db.AbstractItemStore;
import uk.gov.register.db.ItemDAO;
import uk.gov.register.db.ItemQueryDAO;
import uk.gov.register.service.ItemValidator;

import static java.util.Collections.singletonList;

public class InMemoryItemStore extends AbstractItemStore {
    private final ItemDAO itemDAO;
    private final ItemValidator itemValidator;

    public InMemoryItemStore(ItemQueryDAO entryQueryDAO, ItemDAO itemDAO, ItemValidator itemValidator) {
        super(entryQueryDAO);
        this.itemDAO = itemDAO;
        this.itemValidator = itemValidator;
    }

    @Override
    public void putItem(Item item) {
        itemValidator.validateItem(item.getContent());
        itemDAO.insertInBatch(singletonList(item));
    }
}
