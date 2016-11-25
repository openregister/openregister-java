package uk.gov.register.functional;

import org.junit.Before;
import org.junit.ClassRule;
import uk.gov.register.functional.app.RegisterRule;
import uk.gov.register.functional.db.DBSupport;
import uk.gov.register.functional.db.TestDAO;

public class FunctionalTestBase {
    protected static final TestDAO testDAO = TestDAO.get("ft_openregister_java", "postgres");
    protected static final DBSupport dbSupport = new DBSupport(testDAO);
    @ClassRule
    public static RegisterRule register = new RegisterRule("address");

    @Before
    public void setup() {
        register.wipe();
    }

    protected void mintItems(String... items) {
        register.mintLines(items);
    }
}
