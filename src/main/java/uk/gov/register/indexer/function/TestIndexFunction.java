package uk.gov.register.indexer.function;

import uk.gov.register.core.Entry;
import uk.gov.register.indexer.IndexKeyItemPair;

import java.util.Set;

public class TestIndexFunction implements IndexFunction {
    @Override
    public Set<IndexKeyItemPair> execute(Entry entry) {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }
}
