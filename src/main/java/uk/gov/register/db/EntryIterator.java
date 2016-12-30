package uk.gov.register.db;

import org.skife.jdbi.v2.ResultIterator;
import uk.gov.register.core.Entry;

import java.util.function.Function;

public class EntryIterator implements AutoCloseable {

    private final EntryQueryDAO entryDAO;
    private int nextValidEntryNumber;
    private ResultIterator<Entry> iterator;

    private EntryIterator(EntryQueryDAO entryDAO) {
        this.entryDAO = entryDAO;
        this.nextValidEntryNumber = -1;
    }

    public Entry findByEntryNumber(int entryNumber) {
        if (iterator == null || !iterator.hasNext() || entryNumber != nextValidEntryNumber) {
            if (iterator != null) {
                iterator.close();
            }
            iterator = entryDAO.entriesIteratorFrom(entryNumber);
            nextValidEntryNumber = entryNumber;
        }
        nextValidEntryNumber++;
        return iterator.next();
    }

    @Override
    public void close() {
        if (iterator != null) {
            iterator.close();
        }
    }

    public static <R> R withEntryIterator(EntryQueryDAO entryQueryDAO, Function<EntryIterator, R> callback) {
        try (EntryIterator entryIterator = new EntryIterator(entryQueryDAO)) {
            return callback.apply(entryIterator);
        }
    }
}
