package uk.gov.register.core;

import uk.gov.register.db.*;
import uk.gov.register.indexer.IndexDriver;
import uk.gov.register.store.postgres.PostgresDataAccessLayer;

import java.util.HashMap;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;

public class InMemoryItemStore extends ItemStoreImpl {
    private final ItemDAO itemDAO;

    public InMemoryItemStore(ItemQueryDAO itemQueryDAO, ItemDAO itemDAO) {
        super(new PostgresDataAccessLayer(mock(EntryQueryDAO.class), mock(IndexDAO.class), mock(IndexQueryDAO.class), mock(EntryDAO.class),
                mock(EntryItemDAO.class), itemQueryDAO, itemDAO, "schema", mock(IndexDriver.class), new HashMap<>()));
        this.itemDAO = itemDAO;
    }

    @Override
    public void addItem(Item item) {
        itemDAO.insertInBatch(singletonList(item), "schema");
    }
}
