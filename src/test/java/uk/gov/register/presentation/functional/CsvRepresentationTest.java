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
                "{\"hash\":\"hash1\",\"entry\":{\"name\":\"ellis\",\"ft-test-pkey\":\"123,45\"}}",
                "{\"hash\":\"hash2\",\"entry\":{\"name\":\"presley\",\"ft-test-pkey\":\"6789\"}}",
                "{\"hash\":\"hash3\",\"entry\":{\"name\":\"ellis\",\"ft-test-pkey\":\"145678\"}}"
        ));
    }

    @Test
    public void csvRepresentationIsSupportedForEntries() {
        Response response = getRequest("/all.csv");

        assertThat(response.getHeaderString("Content-Type"), equalTo("text/csv;charset=utf-8"));
        assertThat(response.readEntity(String.class), equalTo("hash,name,ft-test-pkey\r\nhash2,presley,6789\r\nhash3,ellis,145678\r\nhash1,ellis,\"123,45\"\r\n"));
    }

    @Test
    public void tsvRepresentationIsSupportedForEntries() {
        Response response = getRequest("/all.tsv");

        assertThat(response.getHeaderString("Content-Type"), equalTo("text/tab-separated-values;charset=utf-8"));
        assertThat(response.readEntity(String.class), equalTo("hash\tname\tft-test-pkey\nhash2\tpresley\t6789\nhash3\tellis\t145678\nhash1\tellis\t123,45\n"));
    }
}
