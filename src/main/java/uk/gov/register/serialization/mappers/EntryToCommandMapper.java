package uk.gov.register.serialization.mappers;

import uk.gov.register.core.Entry;
import uk.gov.register.serialization.RegisterCommand;
import uk.gov.register.serialization.RegisterCommandMapper;
import uk.gov.register.views.RegisterProof;

import java.util.Arrays;
import java.util.Collections;

public class EntryToCommandMapper extends RegisterCommandMapper<Entry,RegisterCommand> {

    @Override
    public RegisterCommand apply(Entry entry) {
        return new RegisterCommand("append-entry", Arrays.asList(entry.getTimestampAsISOFormat(), entry.getSha256hex().encode(), entry.getKey()));
    }
}

