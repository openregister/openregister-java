package uk.gov.register.functional;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import uk.gov.register.functional.app.RegisterRule;
import uk.gov.register.functional.app.TestRegister;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

@RunWith(Parameterized.class)
public class RepresentationsFunctionalTest {
    private final String extension;
    private final String expectedContentType;
    private final String expectedItemValue;
    private final String expectedEntryValue;
    private final String expectedRecordValue;
    private final String expectedRecordsValue;
    private final String expectedEntriesValue;
    private final String expectedRecordEntriesValue;

    @ClassRule
    public static final RegisterRule register = new RegisterRule();

    @Before
    public void publishTestMessages() {
        register.wipe();
        register.loadRsf(TestRegister.register, "add-item\t{\"fields\":[\"field1\"],\"register\":\"value1\",\"text\":\"The Entry 1\"}\n" +
                "add-item\t{\"fields\":[\"field1\",\"field2\"],\"register\":\"value2\",\"text\":\"The Entry 2\"}\n" +
                "append-entry\t2016-03-01T01:02:03Z\tsha-256:877d8bd1ab71dc6e48f64b4ca83c6d7bf645a1eb56b34d50fa8a833e1101eb18\tvalue1\n" +
                "append-entry\t2016-03-02T02:03:04Z\tsha-256:63e5a0453b088e39265ca9f20fd03e2b206422e32989649adaca84426b531cd7\tvalue2\n");
    }

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"csv", "text/csv;charset=UTF-8"},
                {"tsv", "text/tab-separated-values;charset=UTF-8"},
                {"ttl", "text/turtle;charset=UTF-8"},
                {"json", "application/json"},
                {"yaml", "text/yaml;charset=UTF-8"}
        });
    }

    public RepresentationsFunctionalTest(String extension, String expectedContentType) {
        this.extension = extension;
        this.expectedContentType = expectedContentType;
        this.expectedItemValue = fixture("fixtures/item." + extension);
        this.expectedEntryValue = fixture("fixtures/entry." + extension);
        this.expectedRecordValue = fixture("fixtures/record." + extension);
        this.expectedRecordsValue = fixture("fixtures/list." + extension);
        this.expectedEntriesValue = fixture("fixtures/entries." + extension);
        this.expectedRecordEntriesValue = fixture("fixtures/record-entries." + extension);
    }

    @Test
    public void representationIsSupportedForEntryResource() {
        assumeThat(expectedEntryValue, notNullValue());

        Response response = register.getRequest(TestRegister.register, "/entry/1." + extension);

        assertThat(response.getStatus(), equalTo(200));
        assertThat(response.getHeaderString("Content-Type"), equalTo(expectedContentType));
        assertThat(response.readEntity(String.class), equalTo(expectedEntryValue));
    }

    @Test
    public void representationIsSupportedForItemResource() {
        assumeThat(expectedItemValue, notNullValue());

        Response response = register.getRequest(TestRegister.register, "/item/sha-256:877d8bd1ab71dc6e48f64b4ca83c6d7bf645a1eb56b34d50fa8a833e1101eb18." + extension);

        assertThat(response.getStatus(), equalTo(200));
        assertThat(response.getHeaderString("Content-Type"), equalTo(expectedContentType));
        assertThat(response.readEntity(String.class), equalTo(expectedItemValue));
    }

    @Test
    public void representationIsSupportedForRecordResource() {
        assumeThat(expectedRecordValue, notNullValue());

        Response response = register.getRequest(TestRegister.register, "/record/value1." + extension);

        assertThat(response.getStatus(), equalTo(200));
        assertThat(response.getHeaderString("Content-Type"), equalTo(expectedContentType));
        assertThat(response.readEntity(String.class), equalTo(expectedRecordValue));
    }

    @Test
    public void representationIsSupportedForRecordsResource() {
        assumeThat(expectedRecordsValue, notNullValue());

        Response response = register.getRequest(TestRegister.register, "/records." + extension);

        assertThat(response.getStatus(), equalTo(200));
        assertThat(response.getHeaderString("Content-Type"), equalTo(expectedContentType));
        assertThat(response.readEntity(String.class), equalTo(expectedRecordsValue));
    }

    @Test
    public void representationIsSupportedForEntriesResource() {
        assumeThat(expectedEntriesValue, notNullValue());

        Response response = register.getRequest(TestRegister.register, "/entries." + extension);

        assertThat(response.getStatus(), equalTo(200));
        assertThat(response.getHeaderString("Content-Type"), equalTo(expectedContentType));
        assertThat(response.readEntity(String.class), equalTo(expectedEntriesValue));
    }

    @Test
    public void representationIsSupportedForRecordEntriesResource(){
        assumeThat(expectedRecordEntriesValue, notNullValue());

        Response response = register.getRequest(TestRegister.register, "/record/value1/entries." + extension);

        assertThat(response.getStatus(), equalTo(200));
        assertThat(response.getHeaderString("Content-Type"), equalTo(expectedContentType));
        assertThat(response.readEntity(String.class), equalTo(expectedRecordEntriesValue));
    }

    private static String fixture(String resourceName) {
        try {
            return Resources.toString(Resources.getResource(resourceName), Charsets.UTF_8);
        } catch (IOException | IllegalArgumentException e) {
            return null;
        }
    }
}
