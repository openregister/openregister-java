package uk.gov.register.serialization.mappers;

import uk.gov.register.serialization.RegisterCommand;
import uk.gov.register.serialization.RegisterCommandMapper;
import uk.gov.register.util.HashValue;

import java.util.Collections;

public class RootHashCommandMapper extends RegisterCommandMapper<HashValue,RegisterCommand> {

    @Override
    public RegisterCommand apply(HashValue rootHash) {
        return new RegisterCommand("assert-root-hash", Collections.singletonList(rootHash.encode()));
    }
}
