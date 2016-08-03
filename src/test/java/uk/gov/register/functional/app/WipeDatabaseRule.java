package uk.gov.register.functional.app;

import org.junit.rules.ExternalResource;

import static uk.gov.register.functional.db.TestDBSupport.*;

public class WipeDatabaseRule extends ExternalResource {
    @Override
    protected void before() {
        testEntryDAO.wipeData();
        testItemDAO.wipeData();
        testRecordDAO.wipeData();
    }
}

