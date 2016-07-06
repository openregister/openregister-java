package uk.gov.indexer.dao;

import org.skife.jdbi.v2.Handle;

public abstract class ExtendedDestinationDBUpdateDAO extends DestinationDBUpdateDAO {

    private final EntryUpdateDAO entryUpdateDAO;

    public ExtendedDestinationDBUpdateDAO() {
        super();
        Handle handle = getHandle();
        entryUpdateDAO = handle.attach(EntryUpdateDAO.class);
    }

    public int lastReadEntryNumber() {
        return entryUpdateDAO.lastReadEntryNumber();
    }

}
