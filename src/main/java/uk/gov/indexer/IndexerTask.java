package uk.gov.indexer;

import uk.gov.indexer.dao.DestinationDBUpdateDAO;
import uk.gov.indexer.dao.Entry;
import uk.gov.indexer.dao.SourceDBQueryDAO;

import java.util.List;
import java.util.Optional;

public class IndexerTask implements Runnable {
    private final String register;
    private final SourceDBQueryDAO sourceDBQueryDAO;
    private final DestinationDBUpdateDAO destinationDBUpdateDAO;
    private final Optional<AWSCloudSearch> cloudSearch;

    public IndexerTask(String register, SourceDBQueryDAO sourceDBQueryDAO, DestinationDBUpdateDAO destinationDBUpdateDAO, Optional<String> searchDomainEndPoint) {
        this.register = register;
        this.sourceDBQueryDAO = sourceDBQueryDAO;
        this.destinationDBUpdateDAO = destinationDBUpdateDAO;
        this.cloudSearch = searchDomainEndPoint.map(ep -> new AWSCloudSearch(register, ep));
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
        List<Entry> entries;
        while (!(entries = fetchNewEntries()).isEmpty()) {
            destinationDBUpdateDAO.writeEntriesInBatch(register, entries);
            uploadEntriesTocloudSearch(entries);
        }
    }

    private void uploadEntriesTocloudSearch(final List<Entry> entries) {
        cloudSearch.ifPresent(cs -> cs.upload(entries));
    }

    private List<Entry> fetchNewEntries() {
        return sourceDBQueryDAO.read(destinationDBUpdateDAO.lastReadSerialNumber());
    }
}
