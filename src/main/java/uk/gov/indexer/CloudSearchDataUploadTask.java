package uk.gov.indexer;

import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.indexer.dao.EntryUpdateDAO;
import uk.gov.indexer.dao.Record;

import java.util.List;

public class CloudSearchDataUploadTask implements Runnable {
    private final Logger LOGGER = LoggerFactory.getLogger(CloudSearchDataUploadTask.class);

    private final AWSCloudSearch cloudSearch;
    private final String register;
    private EntryUpdateDAO entryUpdateDAO;

    public CloudSearchDataUploadTask(String register, String searchDomainEndPoint, String searchDomainWaterMarkEndPoint, EntryUpdateDAO entryUpdateDAO) {
        this.register = register;
        this.entryUpdateDAO = entryUpdateDAO;
        this.cloudSearch = new AWSCloudSearch(register, searchDomainEndPoint, searchDomainWaterMarkEndPoint);
    }

    @Override
    public void run() {
        try {
            LOGGER.info("Starting upload to cloudsearch domain for register: " + register);
            uploadEntries();
            LOGGER.info("upload to cloudsearch domain completed for register: " + register);
        } catch (Throwable e) {
            LOGGER.error(Throwables.getStackTraceAsString(e));
            throw e;
        }
    }

    private void uploadEntries() {
        List<Record> records;
        int currentWatermark = cloudSearch.currentWaterMark();
        while (!(records = entryUpdateDAO.fetchRecordsAfter(currentWatermark)).isEmpty()) {
            cloudSearch.upload(records);
            currentWatermark = Iterables.getLast(records).entry.getEntryNumber();
            cloudSearch.resetWatermark(currentWatermark);
        }
    }

}
