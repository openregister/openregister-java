package uk.gov.register.resources;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.register.functional.app.RegisterRule;
import uk.gov.register.functional.app.RegistersRuleDisableFollowRedirects;
import uk.gov.register.functional.app.RsfRegisterDefinition;
import uk.gov.register.functional.app.TestRegister;

import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.WILDCARD;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class RedirectResourceTest  {
    @ClassRule
    public static RegisterRule register = new RegistersRuleDisableFollowRedirects();


    @Before
    public void publishTestMessages() throws Throwable {
        register.wipe();
        register.loadRsf(TestRegister.register, RsfRegisterDefinition.REGISTER_REGISTER);
    }

    @Test
    public void getEntryByNumberRedirect() throws Exception {
        Response response = register.getRequest(TestRegister.register, "/entry/1", WILDCARD);
        assertThat(response.getStatus(), equalTo(301));
        assertThat(response.getLocation().getPath(), equalTo("/entries/1"));
    }

    @Test
    public void getItemRedirect() throws Exception {
        Response response = register.getRequest(TestRegister.register, "/item/sha-256:9432331d3343a7ceaaee46308069d01836460294c672223b236727a790acf786", WILDCARD);
        assertThat(response.getStatus(), equalTo(301));
        assertThat(response.getLocation().getPath(), equalTo("/items/sha-256:9432331d3343a7ceaaee46308069d01836460294c672223b236727a790acf786"));
    }

    @Test
    public void getV1Redirect() throws Exception {
        Response response = register.getRequest(TestRegister.register, "/v1", WILDCARD);
        assertThat(response.getStatus(), equalTo(307));
        assertThat(response.getLocation().getPath(), equalTo("/"));
    }

    @Test
    public void getV1ItemRedirect() throws Exception {
        Response response = register.getRequest(TestRegister.register, "/v1/items/sha-256:9432331d3343a7ceaaee46308069d01836460294c672223b236727a790acf786", WILDCARD);
        assertThat(response.getStatus(), equalTo(307));
        assertThat(response.getLocation().getPath(), equalTo("/items/sha-256:9432331d3343a7ceaaee46308069d01836460294c672223b236727a790acf786"));
    }

    @Test
    public void getV1RecordsRedirect() throws Exception {
        Response response = register.getRequest(TestRegister.register, "/v1/records", WILDCARD);
        assertThat(response.getStatus(), equalTo(307));
        assertThat(response.getLocation().getPath(), equalTo("/records"));
    }

    @Test
    public void getV1TrailingSlashRedirect() throws Exception {
        // Trailing slashes are handled by a filter so the immediate request doesn't strip the v1
        Response response = register.getRequest(TestRegister.register, "/v1/records/", WILDCARD);
        assertThat(response.getStatus(), equalTo(301));
        assertThat(response.getLocation().getPath(), equalTo("/v1/records"));
    }

    @Test
    public void getProofRedirect() throws Exception {
        Response response = register.getRequest(TestRegister.register, "/proof/entry/1/2/merkle:sha-256", WILDCARD);
        assertThat(response.getStatus(), equalTo(301));
        assertThat(response.getLocation().getPath(), equalTo("/proof/entries/1/2/merkle:sha-256"));
    }

    @Test
    public void getRecordByKeyRedirect() throws Exception {
        Response response = register.getRequest(TestRegister.register, "/record/6789", WILDCARD);
        assertThat(response.getStatus(), equalTo(301));
        assertThat(response.getLocation().getPath(), equalTo("/records/6789"));
    }

    @Test
    public void getRecordByKeyRedirectRetainsFormat() throws Exception {
        Response response = register.getRequest(TestRegister.register, "/record/6789.json", WILDCARD);
        assertThat(response.getStatus(), equalTo(301));
        assertThat(response.getLocation().getPath(), equalTo("/records/6789.json"));
    }

    @Test
    public void getRecordEntriesRedirect() throws Exception {
        Response response = register.getRequest(TestRegister.register, "/record/6789/entries", WILDCARD);
        assertThat(response.getStatus(), equalTo(301));
        assertThat(response.getLocation().getPath(), equalTo("/records/6789/entries"));
    }

}
