package uk.gov.register.indexer.function;

import uk.gov.register.core.Entry;
import uk.gov.register.indexer.IndexValueItemPair;

import java.util.Set;

public interface IndexFunction {
    Set<IndexValueItemPair> execute(Entry entry);
    String getName();
}
