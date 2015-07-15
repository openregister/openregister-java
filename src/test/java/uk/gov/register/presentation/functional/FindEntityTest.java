package uk.gov.register.presentation.functional;

import com.google.common.collect.ImmutableList;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class FindEntityTest extends FunctionalTestBase {

    @BeforeClass
    public static void publishTestMessages() {
        publishMessages(ImmutableList.of(
                "{\"hash\":\"hash1\",\"entry\":{\"name\":\"ellis\",\"ft_test_pkey\":\"12345\"}}",
                "{\"hash\":\"hash2\",\"entry\":{\"name\":\"presley\",\"ft_test_pkey\":\"6789\"}}",
                "{\"hash\":\"hash3\",\"entry\":{\"name\":\"ellis\",\"ft_test_pkey\":\"145678\"}}"
        ));
    }

    @Test
    public void findByPrimaryKey_shouldReturnEntryWithThPrimaryKey() {
        Response response = getRequest("/ft_test_pkey/12345.json");

        assertThat(response.readEntity(String.class), equalTo("{\"hash\":\"hash1\",\"entry\":{\"name\":\"ellis\",\"ft_test_pkey\":\"12345\"}}"));
    }

    @Test
    public void findByPrimaryKey_returns400ForNonPrimaryKeySearch() {
        Response response = getRequest("/key1/key1Value_1.json");

        assertThat(response.getStatus(), equalTo(400));

    }

    @Test
    public void findByHash_shouldReturnEntryForTheGivenHash() {
        Response response = getRequest("/hash/hash2.json");

        assertThat(response.readEntity(String.class), equalTo("{\"hash\":\"hash2\",\"entry\":{\"name\":\"presley\",\"ft_test_pkey\":\"6789\"}}"));
    }

    @Test
    public void all_shouldReturnAllCurrentVersionsOnly() throws InterruptedException {
        Response response = getRequest("/all.json");

        assertThat(response.readEntity(String.class),
                equalTo("[{\"hash\":\"hash2\",\"entry\":{\"name\":\"presley\",\"ft_test_pkey\":\"6789\"}},{\"hash\":\"hash3\",\"entry\":{\"name\":\"ellis\",\"ft_test_pkey\":\"145678\"}},{\"hash\":\"hash1\",\"entry\":{\"name\":\"ellis\",\"ft_test_pkey\":\"12345\"}}]"));
    }

    @Test
    public void search_returnsAllMatchingEntries() {
        Response response = getRequest("/search.json?name=ellis");

        assertThat(response.readEntity(String.class), equalTo(
                "[{\"hash\":\"hash3\",\"entry\":{\"name\":\"ellis\",\"ft_test_pkey\":\"145678\"}},{\"hash\":\"hash1\",\"entry\":{\"name\":\"ellis\",\"ft_test_pkey\":\"12345\"}}]"
        ));
    }

    @Test
    public void search_returnsAllEntriesWhenNoSearchQueryIsGiven() {
        Response response = getRequest("/search.json");

        assertThat(response.readEntity(String.class), equalTo(
                "[{\"hash\":\"hash2\",\"entry\":{\"name\":\"presley\",\"ft_test_pkey\":\"6789\"}},{\"hash\":\"hash3\",\"entry\":{\"name\":\"ellis\",\"ft_test_pkey\":\"145678\"}},{\"hash\":\"hash1\",\"entry\":{\"name\":\"ellis\",\"ft_test_pkey\":\"12345\"}}]"
        ));
    }

}
