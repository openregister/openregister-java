package uk.gov.register.presentation.functional;

import com.google.common.collect.ImmutableList;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class CsvRepresentationTest extends FunctionalTestBase{
    @BeforeClass
    public static void publishTestMessages() {
        publishMessagesToDB(ImmutableList.of(
                "{\"hash\":\"hash1\",\"entry\":{\"name\":\"ellis\",\"ft_test_pkey\":\"123,45\"}}",
                "{\"hash\":\"hash2\",\"entry\":{\"name\":\"presley\",\"ft_test_pkey\":\"6789\"}}",
                "{\"hash\":\"hash3\",\"entry\":{\"name\":\"ellis\",\"ft_test_pkey\":\"145678\"}}"
        ));
    }

    @Test
    public void csvRepresentationIsSupportedForAnEntry() {
        Response response = getRequest("/hash/hash1.csv");

        assertThat(response.getHeaderString("Content-Type"), equalTo("text/csv; charset=utf-8"));
        assertThat(response.readEntity(String.class), equalTo("hash,name,ft_test_pkey\nhash1,ellis,\"123,45\""));
    }

    @Test
    public void csvRepresentationIsSupportedForEntries() {
        Response response = getRequest("/all.csv");

        assertThat(response.getHeaderString("Content-Type"), equalTo("text/csv; charset=utf-8"));
        assertThat(response.readEntity(String.class), equalTo("hash,name,ft_test_pkey\nhash2,presley,6789\nhash3,ellis,145678\nhash1,ellis,\"123,45\""));
    }

    @Test
    public void tsvRepresentationIsSupportedForAnEntry() {
        Response response = getRequest("/hash/hash1.tsv");

        assertThat(response.getHeaderString("Content-Type"), equalTo("text/tab-separated-values; charset=utf-8"));
        assertThat(response.readEntity(String.class), equalTo("hash\tname\tft_test_pkey\nhash1\tellis\t\"123,45\""));
    }

    @Test
    public void tsvRepresentationIsSupportedForEntries() {
        Response response = getRequest("/all.tsv");

        assertThat(response.getHeaderString("Content-Type"), equalTo("text/tab-separated-values; charset=utf-8"));
        assertThat(response.readEntity(String.class), equalTo("hash\tname\tft_test_pkey\nhash2\tpresley\t6789\nhash3\tellis\t145678\nhash1\tellis\t\"123,45\""));
    }
}
