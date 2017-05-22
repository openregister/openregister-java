package uk.gov.register.serialization.mappers;

import uk.gov.register.core.Entry;
import uk.gov.register.serialization.RegisterCommand;
import uk.gov.register.serialization.RegisterCommandMapper;
import uk.gov.register.util.HashValue;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public class EntryToCommandMapper extends RegisterCommandMapper<Entry, RegisterCommand> {
    @Override
    public RegisterCommand apply(Entry entry) {
        return new RegisterCommand("append-entry", Arrays.asList(entry.getEntryType().name(), entry.getKey(), entry.getTimestampAsISOFormat(), toDelimited(entry.getItemHashes())));
    }

    private String toDelimited(Collection<HashValue> hashValues) {
        return hashValues.stream().map(HashValue::encode).collect(Collectors.joining(";"));
    }

}

