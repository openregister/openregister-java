package uk.gov.register.presentation.functional;

import com.google.common.collect.ImmutableList;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TurtleRepresentationTest extends FunctionalTestBase {
    @BeforeClass
    public static void publishTestMessages() {
        publishMessagesToDB(ImmutableList.of(
                "{\"hash\":\"someHash1\",\"entry\":{\"name\":\"The Entry 1\", \"area\":\"value1\", \"address\":\"12345\"}}",
                "{\"hash\":\"someHash2\",\"entry\":{\"name\":\"The Entry 2\", \"area\":\"value2\", \"address\":\"67890\"}}"
        ));
    }

    public static final String EXPECTED_SINGLE_RECORD = "<http://address.beta.openregister.org/hash/someHash1>\n" +
            " field:area \"value1\" ;\n" +
            " field:address <http://address.openregister.org/address/12345> ;\n" +
            " field:name \"The Entry 1\" .\n";


    public static final String PREFIX ="@prefix field: <http://field.openregister.org/field/>.\n\n";


    public static final String TEXT_TURTLE = "text/turtle;charset=utf-8";

    @Test
    public void turtleRepresentationIsSupportedForSingleEntryView() {
        Response response = getRequest("/hash/someHash1.ttl");

        assertThat(response.getHeaderString("Content-Type"), equalTo(TEXT_TURTLE));
        assertThat(response.readEntity(String.class), equalTo(PREFIX + EXPECTED_SINGLE_RECORD));
    }

    public static final String EXPECTED_LIST_RECORDS =
            "<http://address.beta.openregister.org/hash/someHash2>\n" +
                    " field:area \"value2\" ;\n" +
                    " field:address <http://address.openregister.org/address/67890> ;\n" +
                    " field:name \"The Entry 2\" .\n"
                    + EXPECTED_SINGLE_RECORD;

    @Test
    public void turtleRepresentationIsSupportedForListEntryView() {
        Response response = getRequest("/current.ttl");

        assertThat(response.getHeaderString("Content-Type"), equalTo(TEXT_TURTLE));
        assertThat(response.readEntity(String.class), equalTo(PREFIX + EXPECTED_LIST_RECORDS));
    }
}
