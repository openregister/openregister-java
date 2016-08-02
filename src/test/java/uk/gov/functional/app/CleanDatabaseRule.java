package uk.gov.functional.app;

import org.junit.rules.ExternalResource;

import static uk.gov.functional.db.TestDBSupport.*;

public class CleanDatabaseRule extends ExternalResource {
    @Override
    protected void before() {
        testEntryDAO.dropTable();
        testItemDAO.dropTable();
        testRecordDAO.dropTable();
    }
}

