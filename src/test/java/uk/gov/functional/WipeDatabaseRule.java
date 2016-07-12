package uk.gov.functional;

import org.junit.rules.ExternalResource;

import static uk.gov.functional.TestDBSupport.testEntryDAO;
import static uk.gov.functional.TestDBSupport.testItemDAO;
import static uk.gov.functional.TestDBSupport.testRecordDAO;

public class WipeDatabaseRule extends ExternalResource {
    @Override
    protected void before() {
        testEntryDAO.wipeData();
        testItemDAO.wipeData();
        testRecordDAO.wipeData();
    }
}

