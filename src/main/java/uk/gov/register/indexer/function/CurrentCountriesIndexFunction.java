package uk.gov.register.indexer.function;

import uk.gov.register.core.EntryType;
import uk.gov.register.core.Item;
import uk.gov.register.indexer.IndexKeyItemPair;
import uk.gov.register.util.HashValue;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class CurrentCountriesIndexFunction extends BaseIndexFunction {

    public CurrentCountriesIndexFunction(String name) {
        super(name);
    }

    @Override
    protected void execute(Function<HashValue, Optional<Item>> itemFunc, EntryType type, String key, HashValue itemHash, Set<IndexKeyItemPair> result) {
        itemFunc.apply(itemHash).ifPresent(i -> {
            if (!i.getValue("end-date").isPresent()) {
                result.add(new IndexKeyItemPair(key, i.getSha256hex()));
            }
        });
    }
}
