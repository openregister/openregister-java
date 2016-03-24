package uk.gov.register.presentation.functional.testSupport;

import org.junit.rules.ExternalResource;

public class CleanDatabaseRule extends ExternalResource {
    private TestDAO testDAO;

    public CleanDatabaseRule(TestDAO testDAO) {
        this.testDAO = testDAO;
    }

    @Override
    public void before() throws Throwable {
        testDAO.testEntryIndexDAO.dropTable();
        testDAO.testCurrentKeyDAO.dropTable();
        testDAO.testTotalEntryDAO.dropTable();
        testDAO.testTotalRecordDAO.dropTable();
        testDAO.testItemDAO.dropTable();
        testDAO.testEntryDAO.dropTable();

        testDAO.testEntryIndexDAO.createTable();
        testDAO.testCurrentKeyDAO.createTable();
        testDAO.testTotalEntryDAO.createTable();
        testDAO.testTotalRecordDAO.createTable();
        testDAO.testItemDAO.createTable();
        testDAO.testEntryDAO.createTable();
    }

}
