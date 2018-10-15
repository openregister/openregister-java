package uk.gov.register.indexer.function;

import uk.gov.register.core.Blob;
import uk.gov.register.core.EntryType;
import uk.gov.register.indexer.IndexKeyItemPair;
import uk.gov.register.util.HashValue;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class LocalAuthorityByTypeIndexFunction extends BaseIndexFunction {

    public LocalAuthorityByTypeIndexFunction(String name) {
        super(name);
    }

    @Override
    protected void execute(Function<HashValue, Optional<Blob>> itemFunc, EntryType type, String key, HashValue itemHash, Set<IndexKeyItemPair> result) {
        itemFunc.apply(itemHash).ifPresent(i -> {
            if (i.getValue("local-authority-type").isPresent()) {
                result.add(new IndexKeyItemPair(i.getValue("local-authority-type").get(), i.getSha256hex()));
            }
        });
    }

}
