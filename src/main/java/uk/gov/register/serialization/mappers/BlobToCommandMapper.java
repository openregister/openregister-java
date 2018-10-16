package uk.gov.register.serialization.mappers;

import uk.gov.register.core.Blob;
import uk.gov.register.serialization.RegisterCommand;
import uk.gov.register.serialization.RegisterCommandMapper;
import uk.gov.register.util.CanonicalJsonMapper;

import java.util.Collections;

public class BlobToCommandMapper extends RegisterCommandMapper<Blob,RegisterCommand> {
    private final CanonicalJsonMapper canonicalJsonMapper  = new CanonicalJsonMapper();

    @Override
    public RegisterCommand apply(Blob blob) {
        return new RegisterCommand("add-item", Collections.singletonList(canonicalJsonMapper.writeToString(blob.getContent())));
    }
}

