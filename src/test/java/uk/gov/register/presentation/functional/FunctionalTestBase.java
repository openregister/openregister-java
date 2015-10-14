package uk.gov.register.presentation.functional;

import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import uk.gov.register.presentation.functional.testSupport.CleanDatabaseRule;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

public class FunctionalTestBase {
    public static final String DATABASE_URL = "jdbc:postgresql://localhost:5432/ft_presentation";
    public static final String DATABASE_USER = "postgres";
    public static final int APPLICATION_PORT = 9000;

    private static final String TABLE_NAME = "ordered_entry_index";

    protected static Client client;

    @ClassRule
    public static CleanDatabaseRule cleanDatabaseRule = new CleanDatabaseRule(DATABASE_URL, DATABASE_USER, TABLE_NAME);

    @BeforeClass
    public static void beforeClass() throws InterruptedException {
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");

        client = new JerseyClientBuilder().property("jersey.config.client.followRedirects", false).build();
    }

    Response getRequest(String path) {
        return getRequest("address", path);
    }

    Response getRequest(String registerName, String path) {
        return client.target(String.format("http://localhost:%d%s", APPLICATION_PORT, path)).request().header("Host", registerName + ".beta.openregister.org").get();
    }
}
