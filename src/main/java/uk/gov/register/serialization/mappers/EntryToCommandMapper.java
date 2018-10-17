package uk.gov.register.serialization.mappers;

import uk.gov.register.core.BaseEntry;
import uk.gov.register.serialization.RegisterCommand;
import uk.gov.register.serialization.RegisterCommandMapper;
import uk.gov.register.util.HashValue;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public class EntryToCommandMapper extends RegisterCommandMapper<BaseEntry, RegisterCommand> {
    @Override
    public RegisterCommand apply(BaseEntry entry) {
        return new RegisterCommand("append-entry", Arrays.asList(entry.getEntryType().name(), entry.getKey(), entry.getTimestampAsISOFormat(), toDelimited(entry.getBlobHashes())));
    }

    private String toDelimited(Collection<HashValue> hashValues) {
        return hashValues.stream().map(HashValue::encode).collect(Collectors.joining(";"));
    }

}

