package uk.gov.indexer;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.indexer.dao.DestinationDBUpdateDAO;
import uk.gov.indexer.dao.Entry;
import uk.gov.indexer.dao.Item;
import uk.gov.indexer.dao.SourceDBQueryDAO;

import java.util.List;

public class IndexEntryItemTask implements Runnable {
    private final Logger LOGGER = LoggerFactory.getLogger(IndexEntryItemTask.class);

    private final String register;
    private final DestinationDBUpdateDAO destinationDBUpdateDAO;
    private final SourceDBQueryDAO sourceDBQueryDAO;

    public IndexEntryItemTask(String register, SourceDBQueryDAO sourceDBQueryDAO, DestinationDBUpdateDAO destinationDBUpdateDAO) {
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
            LOGGER.error(ExceptionFormatter.formatExceptionAsString(e));
            throw e;
        }
    }

    protected void update() {
        List<Entry> entries = fetchNewEntries();

        if (!entries.isEmpty()) {
            do {
                int totalNewEntries = entries.size();
                List<Item> items = fetchItemsByHex(Lists.transform(entries, Entry::getItemHash));
                LOGGER.info(String.format("Register '%s': Found '%d' new entries in entry table.", register, totalNewEntries));

                destinationDBUpdateDAO.writeEntriesAndItemsInBatch(entries, items);
                LOGGER.info(String.format("Register '%s': Copied '%d' entries in entry from index '%d'.", register, totalNewEntries, entries.get(0).getEntryNumber()));
            } while (!(entries = fetchNewEntries()).isEmpty());
        }
    }

    private List<Entry> fetchNewEntries() {
        return sourceDBQueryDAO.readEntries(destinationDBUpdateDAO.lastReadEntryNumber());
    }

    private List<Item> fetchItemsByHex(List<String> itemHexValues) {
        return sourceDBQueryDAO.readItems(itemHexValues);
    }
}
