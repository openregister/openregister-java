package uk.gov.functional.db;

import io.dropwizard.jdbi.args.InstantArgumentFactory;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import uk.gov.register.functional.testSupport.TestEntryDAO;

public class TestDAO {
    public final TestCurrentKeyDAO testCurrentKeyDAO;
    public final TestTotalEntryDAO testTotalEntryDAO;
    public final TestTotalRecordDAO testTotalRecordDAO;

    public final uk.gov.register.functional.testSupport.TestItemDAO testItemDAO;
    public final uk.gov.register.functional.testSupport.TestEntryDAO testEntryDAO;

    private TestDAO(String databaseName, String user) {
        String postgresConnectionString = String.format("jdbc:postgresql://localhost:5432/%s?user=%s", databaseName, user);
        DBI dbi = new DBI(postgresConnectionString);
        dbi.registerArgumentFactory(new InstantArgumentFactory());
        Handle handle = dbi.open();
        this.testCurrentKeyDAO = handle.attach(TestCurrentKeyDAO.class);
        this.testTotalEntryDAO = handle.attach(TestTotalEntryDAO.class);
        this.testTotalRecordDAO = handle.attach(TestTotalRecordDAO.class);
        this.testItemDAO = handle.attach(uk.gov.register.functional.testSupport.TestItemDAO.class);
        this.testEntryDAO = handle.attach(TestEntryDAO.class);
    }

    public static TestDAO get(String databaseName, String user) {
        return new TestDAO(databaseName, user);
    }
}

