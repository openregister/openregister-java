package uk.gov.register.serialization.mappers;

import uk.gov.register.core.Entry;
import uk.gov.register.serialization.RegisterCommand;
import uk.gov.register.serialization.RegisterCommandMapper;

import java.util.Arrays;

public class EntryToCommandMapper extends RegisterCommandMapper<Entry, RegisterCommand> {
    @Override
    public RegisterCommand apply(Entry entry) {
        return new RegisterCommand("append-entry", Arrays.asList(entry.getEntryType().name(), entry.getKey(), entry.getTimestampAsISOFormat(), entry.getV1ItemHash().encode()));
    }
}

