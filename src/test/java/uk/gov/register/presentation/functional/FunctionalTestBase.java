package uk.gov.register.presentation.functional;

import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.Before;
import org.junit.BeforeClass;
import uk.gov.register.presentation.functional.testSupport.DBSupport;
import uk.gov.register.presentation.functional.testSupport.TestDAO;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

public class FunctionalTestBase {
    public static final int APPLICATION_PORT = 9000;

    protected static Client client;

    protected static final TestDAO testDAO = TestDAO.get("ft_presentation", "postgres");
    protected static final DBSupport dbSupport = new DBSupport(testDAO);

    @BeforeClass
    public static void beforeClass() throws InterruptedException {
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");

        client = new JerseyClientBuilder().property("jersey.config.client.followRedirects", false).build();
    }

    @Before
    public void resetSchema() {
        testDAO.testCurrentKeyDAO.dropTable();
        testDAO.testTotalEntryDAO.dropTable();
        testDAO.testTotalRecordDAO.dropTable();
        testDAO.testItemDAO.dropTable();
        testDAO.testEntryDAO.dropTable();

        testDAO.testCurrentKeyDAO.createTable();
        testDAO.testTotalEntryDAO.createTable();
        testDAO.testTotalRecordDAO.createTable();
        testDAO.testItemDAO.createTable();
        testDAO.testEntryDAO.createTable();
    }

    Response getRequest(String path) {
        return getRequest("address", path);
    }

    Response getRequest(String registerName, String path) {
        return client.target(String.format("http://localhost:%d%s", APPLICATION_PORT, path)).request().header("Host", registerName + ".beta.openregister.org").get();
    }
}
