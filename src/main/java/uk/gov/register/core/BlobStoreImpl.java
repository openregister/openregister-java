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
        dataAccessLayer.addItem(blob);
    }

    @Override
    public Optional<Blob> getBlob(HashValue hash) {
        return dataAccessLayer.getItem(hash);
    }
    
    @Override
    public Collection<Blob> getAllBlobs() {
        return dataAccessLayer.getAllItems();
    }

    @Override
    public Iterator<Blob> getUserBlobIterator() {
        return dataAccessLayer.getItemIterator(EntryType.user);
    }

    @Override
    public Iterator<Blob> getUserBlobIterator(int startEntryNumber, int endEntryNumber) {
        return dataAccessLayer.getItemIterator(startEntryNumber, endEntryNumber);
    }

    @Override
    public Iterator<Blob> getSystemBlobIterator() {
        return dataAccessLayer.getItemIterator(EntryType.system);
    }
}
