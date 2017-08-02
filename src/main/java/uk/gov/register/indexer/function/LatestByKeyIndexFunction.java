package uk.gov.register.indexer.function;

import uk.gov.register.core.EntryType;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.indexer.IndexKeyItemPair;
import uk.gov.register.util.HashValue;

import java.util.Set;

public class LatestByKeyIndexFunction extends BaseIndexFunction {
    public LatestByKeyIndexFunction(String name) {
        super(name);
    }

    @Override
    protected void execute(RegisterReadOnly register, EntryType type, String key, HashValue itemHash, Set<IndexKeyItemPair> result) {
		result.add(new IndexKeyItemPair(key, itemHash));
    }
}
