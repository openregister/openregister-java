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

        ensureAllTablesExist();
    }

    private void ensureAllTablesExist() {
        this.destinationDBUpdateDAO.ensureIndexedEntriesTableExists();
        this.destinationDBUpdateDAO.ensureWaterMarkTableExists();
        this.destinationDBUpdateDAO.initialiseWaterMarkTableIfRequired();
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
        int currentWaterMark = destinationDBUpdateDAO.currentWaterMark();
        List<byte[]> entries = sourceDBQueryDAO.read(currentWaterMark);
        destinationDBUpdateDAO.writeEntries(entries);
    }
}
