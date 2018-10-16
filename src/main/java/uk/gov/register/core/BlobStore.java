package uk.gov.register.core;

import uk.gov.register.util.HashValue;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

public interface BlobStore {
    void addBlob(Blob blob);

    Optional<Blob> getBlob(HashValue hash);
    
    Collection<Blob> getAllBlobs();

    Iterator<Blob> getUserBlobIterator();

    Iterator<Blob> getUserBlobIterator(int start, int end);
    
    Iterator<Blob> getSystemBlobIterator();
}
