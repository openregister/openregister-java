package uk.gov.register.core;

import uk.gov.register.util.HashValue;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

public interface ItemStore {
    void addItem(Item item);

    Optional<Item> getItemByV1Hash(HashValue hash);
    Optional<Item> getItem(HashValue hash);
    
    Collection<Item> getAllItems();

    Collection<Item> getAllItems(EntryType entryType);

    Collection<Item> getUserItemsPaginated(Optional<HashValue> start, int limit);

    Iterator<Item> getItemIterator(EntryType entryType);

    Iterator<Item> getUserItemIterator(int start, int end);

    int getTotalItems();
}
