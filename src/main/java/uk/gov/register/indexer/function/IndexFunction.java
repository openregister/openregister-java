package uk.gov.register.indexer.function;

import uk.gov.register.core.Entry;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.indexer.IndexKeyItemPair;

import java.util.Set;

public interface IndexFunction {
    Set<IndexKeyItemPair> execute(RegisterReadOnly register, Entry entry);
    String getName();
}
