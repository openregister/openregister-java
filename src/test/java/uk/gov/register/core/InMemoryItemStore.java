package uk.gov.register.core;

import uk.gov.register.db.*;
import uk.gov.register.service.ItemValidator;
import uk.gov.register.store.postgres.PostgresDataAccessLayer;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;

public class InMemoryItemStore extends ItemStoreImpl {
    private final ItemDAO itemDAO;
    private final ItemValidator itemValidator;

    public InMemoryItemStore(ItemQueryDAO itemQueryDAO, ItemDAO itemDAO, ItemValidator itemValidator) {
        super(new PostgresDataAccessLayer(mock(EntryQueryDAO.class), mock(IndexQueryDAO.class), mock(EntryDAO.class),
                mock(EntryItemDAO.class), itemQueryDAO, itemDAO,
                mock(RecordQueryDAO.class), mock(IndexDAO.class), "schema"), itemValidator);
        this.itemDAO = itemDAO;
        this.itemValidator = itemValidator;
    }

    @Override
    public void putItem(Item item) {
        itemValidator.validateItem(item.getContent());
        itemDAO.insertInBatch(singletonList(item), "schema");
    }
}
