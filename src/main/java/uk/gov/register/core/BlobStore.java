package uk.gov.register.core;

import uk.gov.register.util.HashValue;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

public interface BlobStore {
    void addItem(Blob blob);

    Optional<Blob> getItem(HashValue hash);
    
    Collection<Blob> getAllItems();

    Iterator<Blob> getUserItemIterator();

    Iterator<Blob> getUserItemIterator(int start, int end);
    
    Iterator<Blob> getSystemItemIterator();
}
