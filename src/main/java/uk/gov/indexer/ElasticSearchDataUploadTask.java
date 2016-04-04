package uk.gov.indexer;

import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.indexer.dao.IndexedEntriesUpdateDAO;
import uk.gov.indexer.dao.OrderedEntryIndex;

import java.util.List;

public class ElasticSearchDataUploadTask implements Runnable {
    private final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchDataUploadTask.class);

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
            LOGGER.info("Starting upload to elasticsearch domain for register: " + register);
            uploadEntries();
            LOGGER.info("upload to elasticsearch domain completed for register: " + register);
        } catch (Throwable e) {
            LOGGER.error(Throwables.getStackTraceAsString(e));
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
