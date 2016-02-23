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
    private final Optional<CloudwatchRecordsProcessedUpdater> cloudwatchUpdater;

    public IndexerTask(Optional<String> environment, String register, DataSource dataSource, DestinationDBUpdateDAO destinationDBUpdateDAO) {
        this.register = register;
        this.dataSource = dataSource;
        this.destinationDBUpdateDAO = destinationDBUpdateDAO;

        if (environment.isPresent()) {
            this.cloudwatchUpdater = Optional.of(new CloudwatchRecordsProcessedUpdater(environment.get(), register));
        } else {
            this.cloudwatchUpdater = Optional.empty();
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
            long recordsWritten = destinationDBUpdateDAO.writeEntriesInBatch(from, register, fetchResult);
            from = destinationDBUpdateDAO.lastReadSerialNumber();

            if (cloudwatchUpdater.isPresent()) {
                cloudwatchUpdater.get().update(recordsWritten);
            }
        }
        if (noRecordsProcessed && cloudwatchUpdater.isPresent()) {
            cloudwatchUpdater.get().update(0);
        }
    }
}
