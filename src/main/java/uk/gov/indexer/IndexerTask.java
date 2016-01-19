package uk.gov.indexer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.indexer.dao.DestinationDBUpdateDAO;
import uk.gov.indexer.fetchers.FetchResult;
import uk.gov.indexer.fetchers.Fetcher;

public class IndexerTask implements Runnable {
    private final Logger LOGGER = LoggerFactory.getLogger(IndexerTask.class);

    private final String register;
    private final DestinationDBUpdateDAO destinationDBUpdateDAO;
    private final Fetcher fetcher;

    public IndexerTask(String register, Fetcher fetcher, DestinationDBUpdateDAO destinationDBUpdateDAO) {
        this.register = register;
        this.fetcher = fetcher;
        this.destinationDBUpdateDAO = destinationDBUpdateDAO;
    }

    @Override
    public void run() {
        try {
            LOGGER.info("Starting update for: " + register);
            update();
            LOGGER.info("Finished for register: " + register);
        } catch (Throwable e) {
            e.printStackTrace();
            throw e;
        }
    }

    protected void update() {
        FetchResult fetchResult;
        while ((fetchResult = fetchNewEntries()).hasEntries()) {
            destinationDBUpdateDAO.writeEntriesInBatch(register, fetchResult);
        }
    }


    private FetchResult fetchNewEntries() {
        return fetcher.fetch(destinationDBUpdateDAO.lastReadSerialNumber());
    }
}
