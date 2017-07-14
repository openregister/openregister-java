package uk.gov.register.indexer.function;

import uk.gov.register.core.Entry;
import uk.gov.register.core.EntryType;
import uk.gov.register.core.Register;
import uk.gov.register.indexer.IndexKeyItemPair;
import uk.gov.register.util.HashValue;

import java.util.HashSet;
import java.util.Set;

public abstract class BaseIndexFunction implements IndexFunction {

    private final String name;

    public BaseIndexFunction(String name) {
        this.name = name;
    }

    @Override
    public Set<IndexKeyItemPair> execute(Register register, Entry entry) {
        Set<IndexKeyItemPair> result = new HashSet<>();

        entry.getItemHashes().forEach(itemHash -> {
            execute(register, entry.getEntryType(), entry.getKey(), itemHash, result);
        });

        return result;
    }

    protected abstract void execute(Register register, EntryType type, String key, HashValue itemHash, Set<IndexKeyItemPair> result);

    @Override
    public String getName() {
        return name;
    }
}
