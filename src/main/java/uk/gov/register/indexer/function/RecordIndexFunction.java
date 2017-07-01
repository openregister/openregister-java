package uk.gov.register.indexer.function;

import uk.gov.register.core.Register;
import uk.gov.register.indexer.IndexKeyItemPair;
import uk.gov.register.util.HashValue;

import java.util.Set;

public class RecordIndexFunction extends BaseIndexFunction {

    public RecordIndexFunction(String name) {
        super(name);
    }

    @Override
    protected void execute(Register register, String key, HashValue itemHash, Set<IndexKeyItemPair> result) {
        result.add(new IndexKeyItemPair(key, itemHash));
    }
}