package uk.gov.register.core;

import uk.gov.register.util.HashValue;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

public interface ItemStore {
    void putItem(Item item);

    Optional<Item> getItemBySha256(HashValue hash);

    Collection<Item> getAllItems();

    Iterator<Item> getIterator();

    Iterator<Item> getIterator(int start, int end);

    // for transactional item stores, flushes added itemsout to database
    default void checkpoint() {
        // by default, do nothing
    }
}
