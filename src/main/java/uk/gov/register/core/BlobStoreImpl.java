package uk.gov.register.core;

import uk.gov.register.store.DataAccessLayer;
import uk.gov.register.util.HashValue;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

public class BlobStoreImpl implements BlobStore {
    private final DataAccessLayer dataAccessLayer;

    public BlobStoreImpl(DataAccessLayer dataAccessLayer) {
        this.dataAccessLayer = dataAccessLayer;
    }

    @Override
    public void addBlob(Blob blob) {
        dataAccessLayer.addBlob(blob);
    }

    @Override
    public Optional<Blob> getBlob(HashValue hash) {
        return dataAccessLayer.getBlob(hash);
    }
    
    @Override
    public Collection<Blob> getAllBlobs() {
        return dataAccessLayer.getAllBlobs();
    }

    @Override
    public Iterator<Blob> getUserBlobIterator() {
        return dataAccessLayer.getBlobIterator(EntryType.user);
    }

    @Override
    public Iterator<Blob> getUserBlobIterator(int startEntryNumber, int endEntryNumber) {
        return dataAccessLayer.getBlobIterator(startEntryNumber, endEntryNumber);
    }

    @Override
    public Iterator<Blob> getSystemBlobIterator() {
        return dataAccessLayer.getBlobIterator(EntryType.system);
    }
}
