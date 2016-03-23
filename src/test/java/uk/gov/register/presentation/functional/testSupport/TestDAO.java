package uk.gov.register.presentation.functional.testSupport;

import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

public class TestDAO {
    public final TestOrderedEntryIndexDAO testEntryIndexDAO;
    public final TestCurrentKeyDAO testCurrentKeyDAO;
    public final TestTotalEntryDAO testTotalEntryDAO;
    public final TestTotalRecordDAO testTotalRecordDAO;

    private TestDAO(String databaseName, String user) {
        String postgresConnectionString = String.format("jdbc:postgresql://localhost:5432/%s?user=%s", databaseName, user);
        DBI dbi = new DBI(postgresConnectionString);
        Handle handle = dbi.open();
        this.testEntryIndexDAO = handle.attach(TestOrderedEntryIndexDAO.class);
        this.testCurrentKeyDAO = handle.attach(TestCurrentKeyDAO.class);
        this.testTotalEntryDAO = handle.attach(TestTotalEntryDAO.class);
        this.testTotalRecordDAO = handle.attach(TestTotalRecordDAO.class);
    }

    public static TestDAO get(String databaseName, String user) {
        return new TestDAO(databaseName, user);
    }
}

