package uk.gov.register.indexer.function;

import uk.gov.register.core.EntryType;
import uk.gov.register.core.Item;
import uk.gov.register.indexer.IndexKeyItemPair;
import uk.gov.register.util.HashValue;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class LatestByKeyIndexFunction extends BaseIndexFunction {
    public LatestByKeyIndexFunction(String name) {
        super(name);
    }

    @Override
    protected void execute(Function<HashValue, Optional<Item>> itemFunc, EntryType type, String key, HashValue itemHash, Set<IndexKeyItemPair> result) {
		result.add(new IndexKeyItemPair(key, itemHash));
    }
}
