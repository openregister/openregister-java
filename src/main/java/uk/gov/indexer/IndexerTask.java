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
            LOGGER.error(ExceptionFormatter.formatExceptionAsString(e));
            throw e;
        }
    }

    protected void update() {
        List<Entry> entries = fetchNewEntries();

        if (entries.isEmpty()) {
            updateCloudWatch(0);
        } else {
            do {
                int totalNewEntries = entries.size();

                LOGGER.info(String.format("Register '%s': Found '%d' new entries.", register, totalNewEntries));

                destinationDBUpdateDAO.writeEntriesInBatch(register, entries);

                LOGGER.info(String.format("Register '%s': Copied '%d' entries in database from index '%d'.", register, totalNewEntries, entries.get(0).serial_number));

                updateCloudWatch(totalNewEntries);

                LOGGER.info(String.format("Register '%s': Notified Cloudwatch about '%d' entries processed.", register, totalNewEntries));
            } while (!(entries = fetchNewEntries()).isEmpty());
        }

    }

    private void updateCloudWatch(final int totalEntriesWritten) {
        cloudwatchUpdater.ifPresent(cwu -> cwu.update(totalEntriesWritten));
    }

    private List<Entry> fetchNewEntries() {
        return sourceDBQueryDAO.read(destinationDBUpdateDAO.lastReadSerialNumber());
    }

}
