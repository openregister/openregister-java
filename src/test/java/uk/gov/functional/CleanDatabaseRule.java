package uk.gov.functional;

import org.junit.rules.ExternalResource;

import static uk.gov.functional.TestDBSupport.*;

public class CleanDatabaseRule extends ExternalResource {
    @Override
    protected void before() {
        testEntryDAO.dropTable();
        testItemDAO.dropTable();
    }
}

