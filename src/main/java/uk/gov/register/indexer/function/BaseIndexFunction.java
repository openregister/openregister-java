package uk.gov.register.indexer.function;

import uk.gov.register.core.Entry;
import uk.gov.register.indexer.IndexKeyItemPair;
import uk.gov.register.util.HashValue;

import java.util.HashSet;
import java.util.Set;

public abstract class BaseIndexFunction implements IndexFunction {
    @Override
    public Set<IndexKeyItemPair> execute(Entry entry) {
        Set<IndexKeyItemPair> result = new HashSet<>();

        entry.getItemHashes().forEach(itemHash -> {
            execute(entry.getKey(), itemHash, result);
        });

        return result;
    }

    protected abstract void execute(String key, HashValue itemHash, Set<IndexKeyItemPair> result);
}
