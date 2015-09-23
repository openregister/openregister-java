package uk.gov.indexer;

import uk.gov.indexer.dao.DestinationDBUpdateDAO;
import uk.gov.indexer.dao.SourceDBQueryDAO;

import java.util.List;

public class IndexerTask implements Runnable {
    private final String register;
    private final SourceDBQueryDAO sourceDBQueryDAO;
    private final DestinationDBUpdateDAO destinationDBUpdateDAO;

    public IndexerTask(String register, SourceDBQueryDAO sourceDBQueryDAO, DestinationDBUpdateDAO destinationDBUpdateDAO) {
        this.register = register;
        this.sourceDBQueryDAO = sourceDBQueryDAO;
        this.destinationDBUpdateDAO = destinationDBUpdateDAO;

        ensureAllTableExists();
    }

    private void ensureAllTableExists() {
        destinationDBQueryDAO.ensureIndexedEntriesTableExists();

        destinationDBQueryDAO.ensureCurrentKeysTableExists();

        destinationDBQueryDAO.ensureWaterMarkTableExists();
        destinationDBQueryDAO.initialiseWaterMarkTableIfRequired();

        destinationDBQueryDAO.ensureTotalEntriesInRegisterTableExists();
        destinationDBQueryDAO.initialiseTotalEntriesInRegisterIfRequired();
    }

    @Override
    public void run() {
        try {
            update();
            ConsoleLogger.log("Index update completed for register: " + register);
        } catch (Throwable e) {
            e.printStackTrace();
            throw e;
        }
    }

    protected void update() {
        List<byte[]> entries;
        while (!(entries = fetchNewEntries()).isEmpty()) {
            destinationDBQueryDAO.writeEntries(register, entries);
        }
    }

    private List<byte[]> fetchNewEntries() {
        return sourceDBQueryDAO.read(destinationDBQueryDAO.currentWaterMark());
    }
}
