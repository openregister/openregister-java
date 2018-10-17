package uk.gov.register.indexer.function;

import uk.gov.register.core.BaseEntry;
import uk.gov.register.core.EntryType;
import uk.gov.register.core.Blob;
import uk.gov.register.indexer.IndexKeyItemPair;
import uk.gov.register.util.HashValue;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public abstract class BaseIndexFunction implements IndexFunction {

    private final String name;

    public BaseIndexFunction(String name) {
        this.name = name;
    }

    @Override
    public Set<IndexKeyItemPair> execute(Function<HashValue, Optional<Blob>> itemFunc, BaseEntry entry) {
        Set<IndexKeyItemPair> result = new HashSet<>();

        entry.getBlobHashes().forEach(itemHash -> {
            execute(itemFunc, entry.getEntryType(), entry.getKey(), itemHash, result);
        });

        return result;
    }

    protected abstract void execute(Function<HashValue, Optional<Blob>> itemFunc, EntryType type, String key, HashValue itemHash, Set<IndexKeyItemPair> result);

    @Override
    public String getName() {
        return name;
    }
}
