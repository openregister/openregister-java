package uk.gov.register.presentation.functional;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

@RunWith(Parameterized.class)
public class RepresentationsTest extends FunctionalTestBase {
    public static final String REGISTER_NAME = "register";
    private final String extension;
    private final String expectedContentType;
    private final String expectedItemValue;
    private final String expectedEntryValue;
    private final String expectedRecordsValue;
    private final String expectedEntriesValue;

    @Before
    public void publishTestMessages() {
        dbSupport.publishEntries(REGISTER_NAME, ImmutableList.of(
                new TestEntry(1, "{\"fields\":[\"field1\"],\"register\":\"value1\",\"text\":\"The Entry 1\"}",
                        Instant.parse("2016-03-01T01:02:03Z")),
                new TestEntry(2, "{\"fields\":[\"field1\",\"field2\"],\"register\":\"value2\",\"text\":\"The Entry 2\"}",
                        Instant.parse("2016-03-02T02:03:04Z"))
        ));
    }

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
//                {"csv", "text/csv;charset=UTF-8", fixture("fixtures/single.csv"), fixture("fixtures/list.csv")},
//                {"tsv", "text/tab-separated-values;charset=UTF-8", fixture("fixtures/single.tsv"), fixture("fixtures/list.tsv")},
//                {"ttl", "text/turtle;charset=UTF-8", fixture("fixtures/single.ttl"), fixture("fixtures/list.ttl")},
                {"json", "application/json"},
                {"yaml", "text/yaml;charset=UTF-8"}
        });
    }

    public RepresentationsTest(String extension, String expectedContentType) {
        this.extension = extension;
        this.expectedContentType = expectedContentType;
        this.expectedItemValue = fixture("fixtures/item." + extension);
        this.expectedEntryValue = fixture("fixtures/entry." + extension);
        this.expectedRecordsValue = fixture("fixtures/list." + extension);
        this.expectedEntriesValue = fixture("fixtures/entries." + extension);
    }

    @Test
    public void representationIsSupportedForEntryResource() {
        assumeThat(expectedEntryValue, notNullValue());

        Response response = getRequest(REGISTER_NAME, "/entry/1." + extension);

        assertThat(response.getStatus(), equalTo(200));
        assertThat(response.getHeaderString("Content-Type"), equalTo(expectedContentType));
        assertThat(response.readEntity(String.class), equalTo(expectedEntryValue));
    }

    @Test
    public void representationIsSupportedForItemResource() {
        assumeThat(expectedItemValue, notNullValue());

        Response response = getRequest(REGISTER_NAME, "/item/sha-256:877d8bd1ab71dc6e48f64b4ca83c6d7bf645a1eb56b34d50fa8a833e1101eb18." + extension);

        assertThat(response.getStatus(), equalTo(200));
        assertThat(response.getHeaderString("Content-Type"), equalTo(expectedContentType));
        assertThat(response.readEntity(String.class), equalTo(expectedItemValue));
    }

    @Test
    @Ignore("/records doesn't yet support the new yaml format")
    public void representationIsSupportedForRecordsResource() {
        assumeThat(expectedRecordsValue, notNullValue());

        Response response = getRequest(REGISTER_NAME, "/records." + extension);

        assertThat(response.getStatus(), equalTo(200));
        assertThat(response.getHeaderString("Content-Type"), equalTo(expectedContentType));
        assertThat(response.readEntity(String.class), equalTo(expectedRecordsValue));
    }

    @Test
    public void representationIsSupportedForEntriesResource() {
        assumeThat(expectedEntriesValue, notNullValue());

        Response response = getRequest(REGISTER_NAME, "/entries." + extension);

        assertThat(response.getStatus(), equalTo(200));
        assertThat(response.getHeaderString("Content-Type"), equalTo(expectedContentType));
        assertThat(response.readEntity(String.class), equalTo(expectedEntriesValue));
    }

    private static String fixture(String resourceName) {
        try {
            return Resources.toString(Resources.getResource(resourceName), Charsets.UTF_8);
        } catch (IOException | IllegalArgumentException e) {
            return null;
        }
    }
}
