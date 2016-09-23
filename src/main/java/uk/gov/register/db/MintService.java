package uk.gov.register.db;

import com.fasterxml.jackson.databind.JsonNode;
import org.skife.jdbi.v2.TransactionIsolationLevel;
import org.skife.jdbi.v2.sqlobject.Transaction;
import uk.gov.register.core.Item;
import uk.gov.register.core.Register;

import javax.inject.Inject;
import java.util.stream.StreamSupport;

public class MintService  {
    private final Register register;

    @Inject
    public MintService(Register register) {
        this.register = register;
    }

    @Transaction(TransactionIsolationLevel.SERIALIZABLE)
    public void load(Iterable<JsonNode> itemNodes) {
        StreamSupport.stream(itemNodes.spliterator(), false)
                .map(Item::new)
                .forEach(register::mintItem);
    }
}

