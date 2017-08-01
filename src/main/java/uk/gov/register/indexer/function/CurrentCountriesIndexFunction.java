package uk.gov.register.indexer.function;

import uk.gov.register.core.EntryType;
import uk.gov.register.core.Register;
import uk.gov.register.indexer.IndexKeyItemPair;
import uk.gov.register.util.HashValue;

import java.util.Set;

public class CurrentCountriesIndexFunction extends BaseIndexFunction {

    public CurrentCountriesIndexFunction(String name) {
        super(name);
    }

    @Override
    protected void execute(Register register, EntryType type, String key, HashValue itemHash, Set<IndexKeyItemPair> result) {
        register.getItemBySha256(itemHash).ifPresent(i -> {
            if (!i.getValue("end-date").isPresent()) {
                result.add(new IndexKeyItemPair(key, i.getSha256hex()));
            }
        });
    }
}
