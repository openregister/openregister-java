package uk.gov.register.indexer.function;

import uk.gov.register.core.Register;
import uk.gov.register.indexer.IndexKeyItemPair;
import uk.gov.register.util.HashValue;

import java.util.Set;

public class CurrentCountriesIndexFunction extends BaseIndexFunction {
    private Register register;

    public CurrentCountriesIndexFunction() {}

    public CurrentCountriesIndexFunction(Register register) {
        this.register = register;
    }

    @Override
    protected void execute(String key, HashValue itemHash, Set<IndexKeyItemPair> result) {
        register.getItemBySha256(itemHash).ifPresent(i -> {
            if (!i.getValue("end-date").isPresent()) {
                result.add(new IndexKeyItemPair(key, i.getSha256hex()));
            }
        });
    }

    @Override
    public String getName() {
        return "current-countries";
    }
}
