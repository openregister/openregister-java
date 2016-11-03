package uk.gov.register.util;

import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

public class OrphanFinder {

    public static Set<Item> findOrphanItems(Set<Item> items, List<Entry> entries) {
        // much faster calling 'contains' on HashSet than ArrayList
        Set<String> entryHashes = entries.stream().map(Entry::getSha256hex).collect(toSet());
        return items.stream().filter(i -> !entryHashes.contains(i.getSha256hex())).collect(toSet());
    }
}
