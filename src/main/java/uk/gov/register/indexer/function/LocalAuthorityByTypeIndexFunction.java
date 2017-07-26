package uk.gov.register.indexer.function;

import uk.gov.register.core.EntryType;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.indexer.IndexKeyItemPair;
import uk.gov.register.util.HashValue;

import java.util.Set;

public class LocalAuthorityByTypeIndexFunction extends BaseIndexFunction {

    public LocalAuthorityByTypeIndexFunction(String name) {
        super(name);
    }

    @Override
    protected void execute(RegisterReadOnly register, EntryType type, String key, HashValue itemHash, Set<IndexKeyItemPair> result) {
        register.getItemBySha256(itemHash).ifPresent(i -> {
            if (i.getValue("local-authority-type").isPresent()) {
                result.add(new IndexKeyItemPair(i.getValue("local-authority-type").get(), i.getSha256hex()));
            }
        });
    }

}
