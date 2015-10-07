package uk.gov.indexer;

import com.google.common.collect.Iterables;
import uk.gov.indexer.dao.IndexedEntriesUpdateDAO;
import uk.gov.indexer.dao.OrderedEntryIndex;

import java.util.List;

public class CloudSearchDataUploadTask implements Runnable {
    private final AWSCloudSearch cloudSearch;
    private final IndexedEntriesUpdateDAO indexedEntriesUpdateDAO;

    public CloudSearchDataUploadTask(String register, String searchDomainEndPoint, String searchDomainWaterMarkEndPoint, IndexedEntriesUpdateDAO indexedEntriesUpdateDAO) {
        this.indexedEntriesUpdateDAO = indexedEntriesUpdateDAO;
        this.cloudSearch = new AWSCloudSearch(register, searchDomainEndPoint, searchDomainWaterMarkEndPoint);
    }

    @Override
    public void run() {
        try {
            uploadEntries();
        } catch (Throwable e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void uploadEntries() {
        List<OrderedEntryIndex> entries;
        int currentWatermark = cloudSearch.currentWaterMark();
        while (!(entries = indexedEntriesUpdateDAO.fetchEntriesAfter(currentWatermark)).isEmpty()) {
            cloudSearch.upload(entries);
            currentWatermark = Iterables.getLast(entries).getSerial_number();
            cloudSearch.resetWatermark(currentWatermark);
        }
    }

}
