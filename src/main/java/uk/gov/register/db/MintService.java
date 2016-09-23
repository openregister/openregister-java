package uk.gov.register.db;

import com.fasterxml.jackson.databind.JsonNode;
import org.skife.jdbi.v2.TransactionIsolationLevel;
import org.skife.jdbi.v2.sqlobject.Transaction;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;
import uk.gov.register.core.Register;

import javax.inject.Inject;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.StreamSupport;

public class MintService  {
    private final Register register;

    @Inject
    public MintService(Register register) {
        this.register = register;
    }

    @Transaction(TransactionIsolationLevel.SERIALIZABLE)
    public void load(Iterable<JsonNode> itemNodes) {
        AtomicInteger currentEntryNumber = new AtomicInteger(register.getTotalEntries());
        StreamSupport.stream(itemNodes.spliterator(), false)
                .map(Item::new)
                .forEach(item -> {
                    register.addItem(item);
                    register.addEntry(new Entry(currentEntryNumber.incrementAndGet(), item.getSha256hex(), Instant.now()));
                });
    }
}

