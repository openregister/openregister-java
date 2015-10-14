package uk.gov.register.presentation.functional;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import uk.gov.register.presentation.representations.DBSupport;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class RepresentationsTest extends FunctionalTestBase {
    public static final String REGISTER_NAME = "register";
    private final String extension;
    private final String expectedContentType;
    private final String expectedSingleEntry;
    private final String expectedListOfEntries;

    @BeforeClass
    public static void publishTestMessages() {
        DBSupport.publishMessages(REGISTER_NAME, ImmutableList.of(
                "{\"hash\":\"someHash1\",\"entry\":{\"text\":\"The Entry 1\", \"register\":\"value1\", \"fields\":[\"field1\"]}}",
                "{\"hash\":\"someHash2\",\"entry\":{\"text\":\"The Entry 2\", \"register\":\"value2\", \"fields\":[\"field1\",\"field2\"]}}"
        ));
    }

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"csv", "text/csv;charset=utf-8", fixture("fixtures/single.csv"), fixture("fixtures/list.csv")},
                {"tsv", "text/tab-separated-values;charset=utf-8", fixture("fixtures/single.tsv"), fixture("fixtures/list.tsv")},
                {"ttl", "text/turtle;charset=utf-8", fixture("fixtures/single.ttl"), fixture("fixtures/list.ttl")},
                {"yaml", "text/yaml;charset=utf-8", fixture("fixtures/single.yaml"), fixture("fixtures/list.yaml")}
        });
    }

    public RepresentationsTest(String extension, String expectedContentType, String expectedSingleEntry, String expectedListOfEntries) {
        this.extension = extension;
        this.expectedContentType = expectedContentType;
        this.expectedSingleEntry = expectedSingleEntry;
        this.expectedListOfEntries = expectedListOfEntries;
    }

    @Test
    public void representationIsSupportedForSingleEntryView() {
        Response response = getRequest(REGISTER_NAME, "/entry/1." + extension);

        assertThat(response.getStatus(), equalTo(200));
        assertThat(response.getHeaderString("Content-Type"), equalTo(expectedContentType));
        assertThat(response.readEntity(String.class), equalTo(expectedSingleEntry));
    }

    @Test
    public void representationIsSupportedForListEntryView() {
        Response response = getRequest(REGISTER_NAME, "/current." + extension);

        assertThat(response.getStatus(), equalTo(200));
        assertThat(response.getHeaderString("Content-Type"), equalTo(expectedContentType));
        assertThat(response.readEntity(String.class), equalTo(expectedListOfEntries));
    }

    private static String fixture(String resourceName) {
        try {
            return Resources.toString(Resources.getResource(resourceName), Charsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
