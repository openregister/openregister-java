package uk.gov.indexer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.indexer.dao.DestinationDBUpdateDAO;
import uk.gov.indexer.dao.Entry;
import uk.gov.indexer.dao.SourceDBQueryDAO;
import uk.gov.indexer.monitoring.CloudwatchRecordsProcessedUpdater;

import java.util.List;
import java.util.Optional;

public class IndexerTask implements Runnable {
    private final Logger LOGGER = LoggerFactory.getLogger(IndexerTask.class);

    private final String register;
    private final DestinationDBUpdateDAO destinationDBUpdateDAO;
    private final SourceDBQueryDAO sourceDBQueryDAO;
    private final Optional<CloudwatchRecordsProcessedUpdater> cloudwatchUpdater;

    public IndexerTask(Optional<CloudwatchRecordsProcessedUpdater> cloudwatchUpdater, String register, SourceDBQueryDAO sourceDBQueryDAO, DestinationDBUpdateDAO destinationDBUpdateDAO) {
        this.register = register;
        this.sourceDBQueryDAO = sourceDBQueryDAO;
        this.destinationDBUpdateDAO = destinationDBUpdateDAO;
        this.cloudwatchUpdater = cloudwatchUpdater;
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
        List<Entry> newEntries;
        boolean noRecordsProcessed = true;
        while ((newEntries = sourceDBQueryDAO.read(from)).size() > 0) {
            noRecordsProcessed = false;
            destinationDBUpdateDAO.writeEntriesInBatch(register, newEntries);
            from = destinationDBUpdateDAO.lastReadSerialNumber();

            final int totalEntriesWritten = newEntries.size();
            LOGGER.info(String.format("Register '%s': Written %s entries after index '%s'.", register, totalEntriesWritten, from));

            cloudwatchUpdater.ifPresent(cwu -> cwu.update(totalEntriesWritten));
        }

        if (noRecordsProcessed && cloudwatchUpdater.isPresent()) {
            cloudwatchUpdater.get().update(0);
        }
    }
}
