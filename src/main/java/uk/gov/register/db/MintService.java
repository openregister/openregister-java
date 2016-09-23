package uk.gov.register.db;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Iterables;
import org.skife.jdbi.v2.TransactionIsolationLevel;
import org.skife.jdbi.v2.sqlobject.Transaction;
import uk.gov.register.core.Item;
import uk.gov.register.core.Register;

import javax.inject.Inject;

public class MintService  {
    private final Register register;

    @Inject
    public MintService(Register register) {
        this.register = register;
    }

    @Transaction(TransactionIsolationLevel.SERIALIZABLE)
    public void load(Iterable<JsonNode> itemNodes) {
        register.mintItems(Iterables.transform(itemNodes, Item::new));
    }
}

