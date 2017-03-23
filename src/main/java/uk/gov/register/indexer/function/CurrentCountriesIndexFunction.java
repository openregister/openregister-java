package uk.gov.register.indexer.function;

import uk.gov.register.core.Register;
import uk.gov.register.indexer.IndexValueItemPair;
import uk.gov.register.util.HashValue;

import java.util.Set;

public class CurrentCountriesIndexFunction extends BaseIndexFunction {
    private final Register register;

    public CurrentCountriesIndexFunction(Register register) {
        this.register = register;
    }

    @Override
    protected void execute(String key, HashValue itemHash, Set<IndexValueItemPair> result) {
        register.getItemBySha256(itemHash).ifPresent(i -> {
            if (!i.getValue("end-date").isPresent()) {
                result.add(new IndexValueItemPair(key, i.getSha256hex()));
            }
        });
    }

    @Override
    public String getName() {
        return "current-countries";
    }
}
