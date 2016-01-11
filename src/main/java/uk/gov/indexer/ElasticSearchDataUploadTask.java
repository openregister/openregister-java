package uk.gov.indexer;

import com.google.common.collect.Iterables;
import uk.gov.indexer.dao.IndexedEntriesUpdateDAO;
import uk.gov.indexer.dao.OrderedEntryIndex;

import java.util.List;

public class ElasticSearchDataUploadTask implements Runnable {
    private final ElasticSearch elasticsearch;
    private final String register;
    private final IndexedEntriesUpdateDAO indexedEntriesUpdateDAO;

    public ElasticSearchDataUploadTask(String register, String searchDomainEndPoint, IndexedEntriesUpdateDAO indexedEntriesUpdateDAO) {
        this.register = register;
        this.indexedEntriesUpdateDAO = indexedEntriesUpdateDAO;
        this.elasticsearch = new ElasticSearch(register, searchDomainEndPoint);
    }

    @Override
    public void run() {
        try {
            ConsoleLogger.log("Starting upload to elasticsearch domain for register: " + register);
            uploadEntries();
            ConsoleLogger.log("upload to elasticsearch domain completed for register: " + register);
        } catch (Throwable e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void uploadEntries() {
        List<OrderedEntryIndex> entries;
        int currentWatermark = elasticsearch.currentWaterMark();
        while (!(entries = indexedEntriesUpdateDAO.fetchEntriesAfter(currentWatermark)).isEmpty()) {
            elasticsearch.upload(entries);
            currentWatermark = Iterables.getLast(entries).getSerial_number();
        }
    }
}
