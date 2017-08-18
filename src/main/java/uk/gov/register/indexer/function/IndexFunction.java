package uk.gov.register.indexer.function;

import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;
import uk.gov.register.indexer.IndexKeyItemPair;
import uk.gov.register.util.HashValue;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public interface IndexFunction {
    Set<IndexKeyItemPair> execute(Function<HashValue, Optional<Item>> itemFunc, Entry entry);
    String getName();
}
