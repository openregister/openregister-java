package uk.gov.indexer;

import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.indexer.dao.*;
import uk.gov.indexer.monitoring.CloudwatchRecordsProcessedUpdater;

import java.util.List;
import java.util.Optional;

public class IndexerTask implements Runnable {
    private final Logger LOGGER = LoggerFactory.getLogger(IndexerTask.class);

    private Optional<CloudwatchRecordsProcessedUpdater> cloudwatchRecordsProcessedUpdater;
    private final String register;
    private final ExtendedDestinationDBUpdateDAO destinationDBUpdateDAO;
    private final SourceDBQueryDAO sourceDBQueryDAO;

    public IndexerTask(Optional<CloudwatchRecordsProcessedUpdater> cloudwatchRecordsProcessedUpdater, String register, SourceDBQueryDAO sourceDBQueryDAO, ExtendedDestinationDBUpdateDAO destinationDBUpdateDAO) {
        this.cloudwatchRecordsProcessedUpdater = cloudwatchRecordsProcessedUpdater;
        this.register = register;
        this.sourceDBQueryDAO = sourceDBQueryDAO;
        this.destinationDBUpdateDAO = destinationDBUpdateDAO;
    }

    @Override
    public void run() {
        try {
            LOGGER.info("Starting entry/item update for: " + register);
            update();
            LOGGER.info("Finished entry/item for register: " + register);
        } catch (Throwable e) {
            LOGGER.error(Throwables.getStackTraceAsString(e));
            throw e;
        }
    }

    protected void update() {
        List<Record> records = fetchNewRecords();

        if (records.isEmpty()) {
            updateCloudWatch(0);
        } else {
            do {
                int totalNewRecords = records.size();
                LOGGER.info(String.format("Register '%s': Found '%d' new entries in entry table.", register, totalNewRecords));

                destinationDBUpdateDAO.upsertInCurrentKeysTable(register, records);

                updateCloudWatch(totalNewRecords);

                LOGGER.info(String.format("Register '%s': Copied '%d' entries in entry from index '%d'.", register, totalNewRecords, records.get(0).entry.getEntryNumber()));
            } while (!(records = fetchNewRecords()).isEmpty());
        }
    }

    private void updateCloudWatch(final int totalEntriesWritten) {
        cloudwatchRecordsProcessedUpdater.ifPresent(cwu -> cwu.update(totalEntriesWritten));
    }

    private List<Record> fetchNewRecords() {
        return sourceDBQueryDAO.readRecords(destinationDBUpdateDAO.lastReadEntryNumber());
    }
}
