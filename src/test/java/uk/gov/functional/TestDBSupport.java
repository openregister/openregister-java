package uk.gov.functional;

import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import uk.gov.functional.db.TestEntryDAO;
import uk.gov.functional.db.TestItemDAO;

public class TestDBSupport {
    public static String postgresConnectionString = "jdbc:postgresql://localhost:5432/ft_mint?user=postgres";
    public static final TestEntryDAO testEntryDAO;
    public static final TestItemDAO testItemDAO;

    static {
        DBI dbi = new DBI(postgresConnectionString);
        Handle handle = dbi.open();
        testEntryDAO = handle.attach(TestEntryDAO.class);
        testItemDAO = handle.attach(TestItemDAO.class);
    }
}

