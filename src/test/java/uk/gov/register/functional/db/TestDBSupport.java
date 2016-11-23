package uk.gov.register.functional.db;

import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

public class TestDBSupport {
    public static String postgresConnectionString = "jdbc:postgresql://localhost:5432/ft_openregister_java?user=postgres";
    public static final TestEntryDAO testEntryDAO;
    public static final TestItemCommandDAO testItemDAO;
    public static final TestRecordDAO testRecordDAO;

    public static Handle handle;

    static {
        DBI dbi = new DBI(postgresConnectionString);
        handle = dbi.open();
        testEntryDAO = handle.attach(TestEntryDAO.class);
        testItemDAO = handle.attach(TestItemCommandDAO.class);
        testRecordDAO = handle.attach(TestRecordDAO.class);
    }
}

