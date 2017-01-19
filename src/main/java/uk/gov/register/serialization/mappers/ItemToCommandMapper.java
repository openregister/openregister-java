package uk.gov.register.serialization.mappers;

import uk.gov.register.core.Item;
import uk.gov.register.serialization.RegisterCommand;
import uk.gov.register.serialization.RegisterCommandMapper;
import uk.gov.register.util.CanonicalJsonMapper;

import java.util.Collections;

public class ItemToCommandMapper extends RegisterCommandMapper<Item,RegisterCommand> {
    private final CanonicalJsonMapper canonicalJsonMapper  = new CanonicalJsonMapper();

    @Override
    public RegisterCommand apply(Item item) {
        return new RegisterCommand("add-item", Collections.singletonList(canonicalJsonMapper.writeToString(item.getContent())));
    }
}

