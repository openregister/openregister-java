package uk.gov.indexer;

import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.indexer.dao.*;

import java.util.List;

public class IndexEntryItemTask implements Runnable {
    private final Logger LOGGER = LoggerFactory.getLogger(IndexEntryItemTask.class);

    private final String register;
    private final DestinationDBUpdateDAO_NewSchema destinationDBUpdateDAO;
    private final SourceDBQueryDAO sourceDBQueryDAO;

    public IndexEntryItemTask(String register, SourceDBQueryDAO sourceDBQueryDAO, DestinationDBUpdateDAO_NewSchema destinationDBUpdateDAO) {
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

        if (!records.isEmpty()) {
            do {
                int totalNewRecords = records.size();
                LOGGER.info(String.format("Register '%s': Found '%d' new entries in entry table.", register, totalNewRecords));

                destinationDBUpdateDAO.writeEntriesAndItemsInBatch(records);

                LOGGER.info(String.format("Register '%s': Copied '%d' entries in entry from index '%d'.", register, totalNewRecords, records.get(0).entry.getEntryNumber()));
            } while (!(records = fetchNewRecords()).isEmpty());
        }
    }

    private List<Record> fetchNewRecords() {
        return sourceDBQueryDAO.readRecords(destinationDBUpdateDAO.lastReadEntryNumber());
    }
}
