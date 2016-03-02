package uk.gov.functional;

import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import uk.gov.functional.db.TestEntriesDAO;

public class FunctionalTestBase {
    protected static String postgresConnectionString = "jdbc:postgresql://localhost:5432/ft_mint";
    protected static final TestEntriesDAO testEntriesDAO;

    static {
        DBI dbi = new DBI(postgresConnectionString);
        Handle handle = dbi.open();
        testEntriesDAO = handle.attach(TestEntriesDAO.class);
    }
}

