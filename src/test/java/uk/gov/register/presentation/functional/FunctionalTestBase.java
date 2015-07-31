package uk.gov.register.presentation.functional;

import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import uk.gov.register.presentation.dao.PGObjectFactory;
import uk.gov.register.presentation.functional.testSupport.CleanDatabaseRule;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.sql.*;
import java.util.List;

public class FunctionalTestBase {
    public static final String DATABASE_URL = "jdbc:postgresql://localhost:5432/ft_presentation";
    public static final int APPLICATION_PORT = 9000;

    private static final String TABLE_NAME = "ordered_entry_index";

    protected static Client client;

    @ClassRule
    public static CleanDatabaseRule cleanDatabaseRule = new CleanDatabaseRule(DATABASE_URL, TABLE_NAME);

    @BeforeClass
    public static void beforeClass() throws InterruptedException {
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");

        client = new JerseyClientBuilder().build();
    }

    Response getRequest(String path) {
        return client.target(String.format("http://localhost:%d%s", APPLICATION_PORT, path)).request().header("Host","ft-test-pkey.beta.openregister.org").get();
    }



    static void publishMessagesToDB(List<String> messages) {
        org.apache.tomcat.jdbc.pool.DataSource dataSource = new org.apache.tomcat.jdbc.pool.DataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(DATABASE_URL);
        try (Connection connection = dataSource.getConnection()) {
            for (String message : messages) {
                try (PreparedStatement preparedStatement = connection.prepareStatement("Insert into ordered_entry_index(entry) values(?)")) {
                    preparedStatement.setObject(1, PGObjectFactory.jsonbObject(message));
                    preparedStatement.execute();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
