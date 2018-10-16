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
    public void addItem(Blob blob) {
        dataAccessLayer.addItem(blob);
    }

    @Override
    public Optional<Blob> getItem(HashValue hash) {
        return dataAccessLayer.getItem(hash);
    }
    
    @Override
    public Collection<Blob> getAllItems() {
        return dataAccessLayer.getAllItems();
    }

    @Override
    public Iterator<Blob> getUserItemIterator() {
        return dataAccessLayer.getItemIterator(EntryType.user);
    }

    @Override
    public Iterator<Blob> getUserItemIterator(int startEntryNumber, int endEntryNumber) {
        return dataAccessLayer.getItemIterator(startEntryNumber, endEntryNumber);
    }

    @Override
    public Iterator<Blob> getSystemItemIterator() {
        return dataAccessLayer.getItemIterator(EntryType.system);
    }
}
