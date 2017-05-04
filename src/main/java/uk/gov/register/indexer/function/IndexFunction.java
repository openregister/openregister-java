package uk.gov.register.indexer.function;

import uk.gov.register.core.Entry;
import uk.gov.register.core.Register;
import uk.gov.register.indexer.IndexKeyItemPair;

import java.util.Set;

public interface IndexFunction {
    Set<IndexKeyItemPair> execute(Register register, Entry entry);
    String getName();
}
