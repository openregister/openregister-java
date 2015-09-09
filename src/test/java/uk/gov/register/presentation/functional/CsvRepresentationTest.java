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
                "{\"hash\":\"hash1\",\"entry\":{\"name\":\"ellis\",\"address\":\"123,45\"}}",
                "{\"hash\":\"hash2\",\"entry\":{\"name\":\"presley\",\"address\":\"6789\"}}",
                "{\"hash\":\"hash3\",\"entry\":{\"name\":\"ellis\",\"address\":\"145678\"}}"
        ));
    }

    @Test
    public void csvRepresentationIsSupportedForEntries() {
        Response response = getRequest("/current.csv");

        assertThat(response.getHeaderString("Content-Type"), equalTo("text/csv;charset=utf-8"));
        assertThat(response.readEntity(String.class), equalTo("hash,name,address\r\nhash2,presley,6789\r\nhash3,ellis,145678\r\nhash1,ellis,\"123,45\"\r\n"));
    }

    @Test
    public void csvRepresentationIsSupportedForEntry() {
        Response response = getRequest("/hash/hash1.csv");

        assertThat(response.getHeaderString("Content-Type"), equalTo("text/csv;charset=utf-8"));
        assertThat(response.readEntity(String.class), equalTo("hash,name,address\r\nhash1,ellis,\"123,45\"\r\n"));
    }

    @Test
    public void tsvRepresentationIsSupportedForEntries() {
        Response response = getRequest("/current.tsv");

        assertThat(response.getHeaderString("Content-Type"), equalTo("text/tab-separated-values;charset=utf-8"));
        assertThat(response.readEntity(String.class), equalTo("hash\tname\taddress\nhash2\tpresley\t6789\nhash3\tellis\t145678\nhash1\tellis\t123,45\n"));
    }

    @Test
    public void tsvRepresentationIsSupportedForEntry() {
        Response response = getRequest("/hash/hash1.tsv");

        assertThat(response.getHeaderString("Content-Type"), equalTo("text/tab-separated-values;charset=utf-8"));
        assertThat(response.readEntity(String.class), equalTo("hash\tname\taddress\nhash1\tellis\t123,45\n"));
    }
}
