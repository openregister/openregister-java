package uk.gov.register.core;

import uk.gov.register.db.*;
import uk.gov.register.indexer.IndexDriver;
import uk.gov.register.store.postgres.PostgresDataAccessLayer;

import java.util.HashMap;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;

public class InMemoryBlobStore extends BlobStoreImpl {
    private final BlobDAO blobDAO;

    public InMemoryBlobStore(BlobQueryDAO blobQueryDAO, BlobDAO blobDAO) {
        super(new PostgresDataAccessLayer(mock(EntryQueryDAO.class), mock(IndexDAO.class), mock(IndexQueryDAO.class), mock(EntryDAO.class),
                mock(EntryBlobDAO.class), blobQueryDAO, blobDAO, "schema", mock(IndexDriver.class), new HashMap<>()));
        this.blobDAO = blobDAO;
    }

    @Override
    public void addBlob(Blob blob) {
        blobDAO.insertInBatch(singletonList(blob), "schema");
    }
}
