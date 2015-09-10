package uk.gov.register.presentation.functional;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class RepresentationsTest extends FunctionalTestBase {
    private final String extension;
    private final String expectedContentType;
    private final String expectedSingleRecord;
    private final String expectedListRecords;

    @BeforeClass
    public static void publishTestMessages() {
        publishMessagesToDB(ImmutableList.of(
                "{\"hash\":\"someHash1\",\"entry\":{\"name\":\"The Entry 1\", \"area\":\"value1\", \"address\":\"12345\"}}",
                "{\"hash\":\"someHash2\",\"entry\":{\"name\":\"The Entry 2\", \"area\":\"value2\", \"address\":\"67890\"}}"
        ));
    }

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"ttl", "text/turtle;charset=utf-8", fixture("fixtures/single.ttl"), fixture("fixtures/list.ttl")},
                {"yaml", "text/yaml;charset=utf-8", fixture("fixtures/single.yaml"), fixture("fixtures/list.yaml")}
        });
    }

    public RepresentationsTest(String extension, String expectedContentType, String expectedSingleRecord, String expectedListRecords) {
        this.extension = extension;
        this.expectedContentType = expectedContentType;
        this.expectedSingleRecord = expectedSingleRecord;
        this.expectedListRecords = expectedListRecords;
    }

    @Test
    public void representationIsSupportedForSingleEntryView() {
        Response response = getRequest("/hash/someHash1." + extension);

        assertThat(response.getStatus(), equalTo(200));
        assertThat(response.getHeaderString("Content-Type"), equalTo(expectedContentType));
        assertThat(response.readEntity(String.class), equalTo(expectedSingleRecord));
    }

    @Test
    public void representationIsSupportedForListEntryView() {
        Response response = getRequest("/current." + extension);

        assertThat(response.getStatus(), equalTo(200));
        assertThat(response.getHeaderString("Content-Type"), equalTo(expectedContentType));
        assertThat(response.readEntity(String.class), equalTo(expectedListRecords));
    }

    private static String fixture(String resourceName) {
        try {
            return Resources.toString(Resources.getResource(resourceName), Charsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
