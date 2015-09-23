package uk.gov.indexer;

import uk.gov.indexer.dao.DestinationDBQueryDAO;
import uk.gov.indexer.dao.SourceDBQueryDAO;

import java.util.List;

public class IndexerTask implements Runnable {
    private final String register;
    private final SourceDBQueryDAO sourceDBQueryDAO;
    private final DestinationDBQueryDAO destinationDBQueryDAO;

    public IndexerTask(String register, SourceDBQueryDAO sourceDBQueryDAO, DestinationDBQueryDAO destinationDBQueryDAO) {
        this.register = register;
        this.sourceDBQueryDAO = sourceDBQueryDAO;
        this.destinationDBQueryDAO = destinationDBQueryDAO;

        ensureAllTablesExists();
    }

    private void ensureAllTablesExists() {
        this.destinationDBQueryDAO.ensureIndexedEntriesTableExists();
        this.destinationDBQueryDAO.ensureWaterMarkTableExists();
        this.destinationDBQueryDAO.initialiseWaterMarkTableIfRequired();
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
        int currentWaterMark = destinationDBQueryDAO.currentWaterMark();
        //write while loop
        List<byte[]> entries = sourceDBQueryDAO.read(currentWaterMark);
        destinationDBQueryDAO.writeEntries(entries);
    }
}
