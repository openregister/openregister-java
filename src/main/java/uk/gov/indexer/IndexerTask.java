package uk.gov.indexer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.indexer.dao.DestinationDBUpdateDAO;
import uk.gov.indexer.fetchers.DataSource;
import uk.gov.indexer.fetchers.FetchResult;
import uk.gov.indexer.monitoring.CloudwatchRecordsProcessedUpdater;

import java.util.Optional;

public class IndexerTask implements Runnable {
    private final Logger LOGGER = LoggerFactory.getLogger(IndexerTask.class);

    private final String register;
    private final DestinationDBUpdateDAO destinationDBUpdateDAO;
    private final DataSource dataSource;
    private final CloudwatchRecordsProcessedUpdater cloudwatchUpdater;

    public IndexerTask(Optional<String> environment, String register, DataSource dataSource, DestinationDBUpdateDAO destinationDBUpdateDAO) {
        this.register = register;
        this.dataSource = dataSource;
        this.destinationDBUpdateDAO = destinationDBUpdateDAO;

        if (environment.isPresent()) {
            this.cloudwatchUpdater = new CloudwatchRecordsProcessedUpdater(environment.get(), register);
        } else {
            this.cloudwatchUpdater = null;
        }
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
        int from = destinationDBUpdateDAO.lastReadSerialNumber();
        FetchResult fetchResult;
        boolean noRecordsProcessed = true;
        while ((fetchResult = dataSource.fetchCurrentSnapshot()).hasMoreEntries(from)) {
            noRecordsProcessed = false;
            destinationDBUpdateDAO.writeEntriesInBatch(from, register, fetchResult);
            int lastUpdatedSerialNumber = destinationDBUpdateDAO.lastReadSerialNumber();
            from = lastUpdatedSerialNumber;

            if (cloudwatchUpdater != null) {
                cloudwatchUpdater.update(lastUpdatedSerialNumber - from);
            }
        }
        if (noRecordsProcessed) {
            cloudwatchUpdater.update(0);
        }
    }
}
