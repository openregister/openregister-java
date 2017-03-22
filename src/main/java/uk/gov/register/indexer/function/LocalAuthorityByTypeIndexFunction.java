package uk.gov.register.indexer.function;

import uk.gov.register.core.Register;
import uk.gov.register.indexer.IndexValueItemPair;
import uk.gov.register.util.HashValue;

import java.util.Set;

public class LocalAuthorityByTypeIndexFunction extends BaseIndexFunction {
    private final Register register;

    public LocalAuthorityByTypeIndexFunction(Register register) {
        this.register = register;
    }

    @Override
    protected void execute(String key, HashValue itemHash, Set<IndexValueItemPair> result) {
        register.getItemBySha256(itemHash).map(i -> result.add(new IndexValueItemPair(i.getValue("local-authority-type"), i.getSha256hex())));
    }

    @Override
    public String getName() {
        return "local-authority-by-type";
    }
}
