package uk.gov.register.core;

import uk.gov.register.db.*;
import uk.gov.register.store.postgres.BatchedPostgresDataAccessLayer;
import uk.gov.register.store.postgres.PostgresDataAccessLayer;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;

public class InMemoryItemStore extends ItemStoreImpl {
    private final ItemDAO itemDAO;

    public InMemoryItemStore(ItemQueryDAO itemQueryDAO, ItemDAO itemDAO) {
        super(new BatchedPostgresDataAccessLayer(new PostgresDataAccessLayer(mock(EntryDAO.class), mock(EntryQueryDAO.class),
                itemDAO, itemQueryDAO, mock(RecordQueryDAO.class),"schema")));
        this.itemDAO = itemDAO;
    }

    @Override
    public void addItem(Item item) {
        itemDAO.insertInBatch(singletonList(item), "schema");
    }
}
