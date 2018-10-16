package uk.gov.register.functional;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.register.functional.app.RegisterRule;
import uk.gov.register.functional.app.RsfRegisterDefinition;

import javax.ws.rs.core.Response;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.register.functional.app.TestRegister.address;

public class PreviewBlobsResourceFunctionalTest {

    @ClassRule
    public static RegisterRule register = new RegisterRule();

    @Before
    public void publishTestMessages() throws Throwable {
        register.wipe();
        register.loadRsf(address, RsfRegisterDefinition.ADDRESS_FIELDS + RsfRegisterDefinition.ADDRESS_REGISTER + addressRsf());
    }

    @Test
    public void getJsonPreview() throws IOException {
        final Response response = register.getRequest(address, "/preview-items/json/sha-256:9432331d3343a7ceaaee46308069d01836460294c672223b236727a790acf786");
        final String entity;

        assertThat(response.getStatus(), equalTo(200));

        assertThat(response.getMediaType().getType(), equalTo("text"));
        assertThat(response.getMediaType().getSubtype(), equalTo("html"));

        entity = response.readEntity(String.class);

        assertThat(entity, containsString("JSON"));
    }

    @Test
    public void getYamlPreview() throws IOException {
        final Response response = register.getRequest(address, "/preview-items/yaml/sha-256:9432331d3343a7ceaaee46308069d01836460294c672223b236727a790acf786");
        final String entity;

        assertThat(response.getStatus(), equalTo(200));

        assertThat(response.getMediaType().getType(), equalTo("text"));
        assertThat(response.getMediaType().getSubtype(), equalTo("html"));

        entity = response.readEntity(String.class);

        assertThat(entity, containsString("YAML"));
    }

    @Test
    public void getTtlPreview() throws IOException {
        final Response response = register.getRequest(address, "/preview-items/turtle/sha-256:9432331d3343a7ceaaee46308069d01836460294c672223b236727a790acf786");
        final String entity;

        assertThat(response.getStatus(), equalTo(200));

        assertThat(response.getMediaType().getType(), equalTo("text"));
        assertThat(response.getMediaType().getSubtype(), equalTo("html"));

        entity = response.readEntity(String.class);

        assertThat(entity, containsString("TTL"));
    }

    private String addressRsf() {
        return "add-item\t{\"address\":\"6789\",\"street\":\"elvis\"}\n" +
                "append-entry\tuser\t6789\t2017-06-09T12:09:02Z\tsha-256:9432331d3343a7ceaaee46308069d01836460294c672223b236727a790acf786\n" +
                "add-item\t{\"address\":\"6789\",\"street\":\"presley\"}\n" +
                "append-entry\tuser\t6789\t2017-06-09T12:09:02Z\tsha-256:bd239db51960376826b937a615f0f3397485f00611d35bb7e951e357bf73b934\n" +
                "add-item\t{\"address\":\"145678\",\"street\":\"ellis\"}\n" +
                "append-entry\tuser\t145678\t2017-06-09T12:09:02Z\tsha-256:8ac926428ee49fb83c02bdd2556e62e84cfd9e636cd35eb1306ac8cb661e4983";
    }
}
