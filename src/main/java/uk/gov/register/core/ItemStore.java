package uk.gov.register.core;

import uk.gov.register.util.HashValue;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

public interface ItemStore {
    void addItem(Item item);

    Optional<Item> getItem(HashValue hash);
    
    Collection<Item> getAllItems();

    Iterator<Item> getUserItemIterator();

    Iterator<Item> getUserItemIterator(int start, int end);
    
    Iterator<Item> getSystemItemIterator();
}
