package uk.gov.register.proofs;

import org.skife.jdbi.v2.ResultIterator;
import uk.gov.register.core.Entry;
import uk.gov.register.db.EntryQueryDAO;

import java.util.function.Function;

public class EntryIterator implements AutoCloseable {

    private final EntryQueryDAO entryDAO;
    private final String schema;
    private int nextValidEntryNumber;
    private ResultIterator<Entry> iterator;

    private EntryIterator(EntryQueryDAO entryDAO, String schema) {
        this.entryDAO = entryDAO;
        this.schema = schema;
        this.nextValidEntryNumber = -1;

    }

    public Entry findByEntryNumber(int entryNumber) {
        if (iterator == null || !iterator.hasNext() || entryNumber != nextValidEntryNumber) {
            if (iterator != null) {
                iterator.close();
            }
            iterator = entryDAO.entriesIteratorFrom(entryNumber, schema);
            nextValidEntryNumber = entryNumber;
        }
        nextValidEntryNumber++;
        return iterator.next();
    }

    public int getTotalEntries() {
        return entryDAO.getTotalEntries(schema);
    }

    @Override
    public void close() {
        if (iterator != null) {
            iterator.close();
        }
    }

    public static <R> R withEntryIterator(EntryQueryDAO entryQueryDAO, Function<EntryIterator, R> callback, String schema) {
        try (EntryIterator entryIterator = new EntryIterator(entryQueryDAO, schema)) {
            return callback.apply(entryIterator);
        }
    }
}
