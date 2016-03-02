package uk.gov.functional;

import org.junit.rules.ExternalResource;
import uk.gov.functional.db.TestEntriesDAO;

public class CleanDatabaseRule extends ExternalResource {
    public final TestEntriesDAO testEntriesDAO;

    public CleanDatabaseRule(TestEntriesDAO testEntriesDAO) {
        this.testEntriesDAO = testEntriesDAO;
    }

    @Override
    protected void before() {
        testEntriesDAO.dropTable();
    }
}

