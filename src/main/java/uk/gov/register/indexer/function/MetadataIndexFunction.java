package uk.gov.register.indexer.function;

import uk.gov.register.core.EntryType;
import uk.gov.register.core.Register;
import uk.gov.register.indexer.IndexKeyItemPair;
import uk.gov.register.util.HashValue;

import java.util.Set;

public class MetadataIndexFunction extends BaseIndexFunction {
    public MetadataIndexFunction(String name) {
        super(name);
    }

    @Override
    protected void execute(Register register, EntryType type, String key, HashValue itemHash, Set<IndexKeyItemPair> result) {
        if (type == EntryType.system) {
            result.add(new IndexKeyItemPair(key, itemHash));
        }
    }
}
