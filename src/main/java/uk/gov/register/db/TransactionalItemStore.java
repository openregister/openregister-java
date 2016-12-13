package uk.gov.register.db;

import uk.gov.register.core.Item;
import uk.gov.register.service.ItemValidator;
import uk.gov.register.util.HashValue;

import java.util.HashMap;
import java.util.Map;

public class TransactionalItemStore extends AbstractItemStore {
    private final ItemDAO itemDAO;
    private final ItemValidator itemValidator;
    private final Map<HashValue, Item> stagedItems;

    public TransactionalItemStore(ItemDAO itemDAO, ItemQueryDAO itemQueryDAO, ItemValidator itemValidator) {
        super(itemQueryDAO);
        this.itemDAO = itemDAO;
        this.itemValidator = itemValidator;
        this.stagedItems = new HashMap<>();
    }

    @Override
    public void putItem(Item item) {
        itemValidator.validateItem(item.getContent());
        stagedItems.put(item.getSha256hex(), item);
    }

    public void checkpoint() {
        if (stagedItems.isEmpty()) {
            return;
        }
        itemDAO.insertInBatch(stagedItems.values());
        stagedItems.clear();
    }
}
