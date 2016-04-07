package uk.gov.register.presentation.functional.testSupport;

import io.dropwizard.java8.jdbi.args.InstantArgumentFactory;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import java.util.Optional;

public class TestDAO {
    public final TestOrderedEntryIndexDAO testEntryIndexDAO;
    public final TestCurrentKeyDAO testCurrentKeyDAO;
    public final TestTotalEntryDAO testTotalEntryDAO;
    public final TestTotalRecordDAO testTotalRecordDAO;

    public final TestItemDAO testItemDAO;
    public final TestEntryDAO testEntryDAO;

    private TestDAO(String databaseName, String user) {
        String postgresConnectionString = String.format("jdbc:postgresql://localhost:5432/%s?user=%s", databaseName, user);
        DBI dbi = new DBI(postgresConnectionString);
        dbi.registerArgumentFactory(new InstantArgumentFactory(Optional.empty()));
        Handle handle = dbi.open();
        this.testEntryIndexDAO = handle.attach(TestOrderedEntryIndexDAO.class);
        this.testCurrentKeyDAO = handle.attach(TestCurrentKeyDAO.class);
        this.testTotalEntryDAO = handle.attach(TestTotalEntryDAO.class);
        this.testTotalRecordDAO = handle.attach(TestTotalRecordDAO.class);
        this.testItemDAO = handle.attach(TestItemDAO.class);
        this.testEntryDAO = handle.attach(TestEntryDAO.class);
    }

    public static TestDAO get(String databaseName, String user) {
        return new TestDAO(databaseName, user);
    }
}

