package uk.gov.register.functional.db;

import io.dropwizard.jdbi.args.InstantArgumentFactory;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

public class TestDAO {
    public final TestCurrentKeyDAO testCurrentKeyDAO;
    public final TestTotalEntryDAO testTotalEntryDAO;
    public final TestTotalRecordDAO testTotalRecordDAO;

    public final TestItemQueryDAO testItemDAO;
    public final TestEntryDAO testEntryDAO;

    public final String postgresConnectionString;

    private TestDAO(String databaseName, String user) {
        this.postgresConnectionString = String.format("jdbc:postgresql://localhost:5432/%s?user=%s", databaseName, user);
        DBI dbi = new DBI(postgresConnectionString);
        dbi.registerArgumentFactory(new InstantArgumentFactory());
        Handle handle = dbi.open();
        this.testCurrentKeyDAO = handle.attach(TestCurrentKeyDAO.class);
        this.testTotalEntryDAO = handle.attach(TestTotalEntryDAO.class);
        this.testTotalRecordDAO = handle.attach(TestTotalRecordDAO.class);
        this.testItemDAO = handle.attach(TestItemQueryDAO.class);
        this.testEntryDAO = handle.attach(TestEntryDAO.class);
    }

    public static TestDAO get(String databaseName, String user) {
        return new TestDAO(databaseName, user);
    }
}

