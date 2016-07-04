package uk.gov.register.presentation.dao;

import org.skife.jdbi.v2.ResultIterator;

public class EntryIteratorDAO {

    private final EntryDAO entryDAO;
    private int nextValidEntryNumber;
    private ResultIterator<Entry> iterator;

    public EntryIteratorDAO(EntryDAO entryDAO) {
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
