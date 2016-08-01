package uk.gov.register.db;

import org.skife.jdbi.v2.ResultIterator;
import uk.gov.register.core.Entry;

public class EntryIteratorDAO {

    private final EntryQueryDAO entryDAO;
    private int nextValidEntryNumber;
    private ResultIterator<Entry> iterator;

    public EntryIteratorDAO(EntryQueryDAO entryDAO) {
        this.entryDAO = entryDAO;
        this.nextValidEntryNumber = -1;
    }

    public Entry findByEntryNumber(int entryNumber) {
        if (iterator == null || entryNumber != nextValidEntryNumber) {
            if (iterator != null) {
                iterator.close();
            }
            iterator = entryDAO.entriesIteratorFrom(entryNumber);
            nextValidEntryNumber = entryNumber;
        }
        nextValidEntryNumber++;
        return iterator.next();
    }
}
