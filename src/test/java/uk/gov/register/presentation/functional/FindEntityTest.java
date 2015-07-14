package uk.gov.register.presentation.functional;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class FindEntityTest extends FunctionalTestBase {
    @Test
    public void findByPrimaryKey_shouldReturnEntryWithThPrimaryKey() {
        publishMessages(ImmutableList.of(
                "{\"hash\":\"entryHash1\", \"entry\":{\"ft_test_pkey\":\"ft_test_pkey_value_1\", \"key1\":\"key1Value_1\"}}",
                "{\"hash\":\"entryHash2\", \"entry\":{\"ft_test_pkey\":\"ft_test_pkey_value_2\", \"key1\":\"key1Value_2\"}}",
                "{\"hash\":\"entryHash3\", \"entry\":{\"ft_test_pkey\":\"ft_test_pkey_value_1\", \"key1\":\"key1Value_3\"}}"
        ));

        Response response = getRequest("/ft_test_pkey/ft_test_pkey_value_1");

        assertThat(response.readEntity(String.class), equalTo("{\"hash\":\"entryHash3\",\"entry\":{\"key1\":\"key1Value_3\",\"ft_test_pkey\":\"ft_test_pkey_value_1\"}}"));
    }

    @Test
    public void findByPrimaryKey_returns400ForNonPrimaryKeySearch() {
        publishMessages(ImmutableList.of("{\"hash\":\"entryHash1\", \"entry\":{\"ft_test_pkey\":\"ft_test_pkey_value_1\", \"key1\":\"key1Value_1\"}}"));
        Response response = getRequest("/key1/key1Value_1");

        assertThat(response.getStatus(), equalTo(400));

    }

    @Test
    public void findByHash_shouldReturnEntryForTheGivenHash() {
        publishMessages(ImmutableList.of(
                "{\"hash\":\"entryHash1\", \"entry\":{\"ft_test_pkey\":\"ft_test_pkey_value_1\", \"key1\":\"key1Value_1\"}}",
                "{\"hash\":\"entryHash2\", \"entry\":{\"ft_test_pkey\":\"ft_test_pkey_value_2\", \"key1\":\"key1Value_2\"}}"
        ));

        Response response = getRequest("/hash/entryHash2");

        assertThat(response.readEntity(String.class), equalTo("{\"hash\":\"entryHash2\",\"entry\":{\"key1\":\"key1Value_2\",\"ft_test_pkey\":\"ft_test_pkey_value_2\"}}"));
    }

}
