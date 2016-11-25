package uk.gov.register.functional;

import uk.gov.register.functional.db.DBSupport;
import uk.gov.register.functional.db.TestDAO;

public class FunctionalTestBase {
    protected static final TestDAO testDAO = TestDAO.get("ft_openregister_java", "postgres");
    protected static final DBSupport dbSupport = new DBSupport(testDAO);
}
