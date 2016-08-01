package uk.gov.functional;

import org.junit.rules.ExternalResource;

import static uk.gov.functional.TestDBSupport.*;

public class WipeDatabaseRule extends ExternalResource {
    @Override
    protected void before() {
        testEntryDAO.wipeData();
        testItemDAO.wipeData();
        testRecordDAO.wipeData();
    }
}

