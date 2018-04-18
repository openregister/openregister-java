package uk.gov.register.resources;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.register.functional.app.RegisterRule;
import uk.gov.register.functional.app.RegistersRuleDisableFollowRedirects;
import uk.gov.register.functional.app.RsfRegisterDefinition;

import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.WILDCARD;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.register.functional.app.TestRegister.address;

public class RedirectResourceTest  {
    @ClassRule
    public static RegisterRule register = new RegistersRuleDisableFollowRedirects();

    private String addressRsf(){
        return "add-item\t{\"address\":\"6789\",\"street\":\"elvis\"}\n" +
                "append-entry\tuser\t6789\t2017-06-09T10:23:22Z\tsha-256:9432331d3343a7ceaaee46308069d01836460294c672223b236727a790acf786\n" +
                "add-item\t{\"address\":\"6790\",\"street\":\"presley\"}\n" +
                "append-entry\tuser\t6790\t2017-06-09T10:23:22Z\tsha-256:fdd8a3c301f1e8d117ce284d4e67f3b797f4dc573c8d40de502f540709f03007";
    }

    @Before
    public void publishTestMessages() throws Throwable {
        register.wipe();
        register.loadRsf(address, RsfRegisterDefinition.ADDRESS_FIELDS + RsfRegisterDefinition.ADDRESS_REGISTER + addressRsf());
    }

    @Test
    public void getEntryByNumberRedirect() throws Exception {
        Response response = register.getRequest(address, "/entry/1", WILDCARD);
        assertThat(response.getStatus(), equalTo(301));
        assertThat(response.getLocation().getPath(), equalTo("/entries/1"));
    }

    @Test
    public void getItemRedirect() throws Exception {
        Response response = register.getRequest(address, "/item/sha-256:9432331d3343a7ceaaee46308069d01836460294c672223b236727a790acf786", WILDCARD);
        assertThat(response.getStatus(), equalTo(301));
        assertThat(response.getLocation().getPath(), equalTo("/items/sha-256:9432331d3343a7ceaaee46308069d01836460294c672223b236727a790acf786"));
    }

    @Test
    public void getProofRedirect() throws Exception {
        Response response = register.getRequest(address, "/proof/entry/1/2/merkle:sha-256", WILDCARD);
        assertThat(response.getStatus(), equalTo(301));
        assertThat(response.getLocation().getPath(), equalTo("/proof/entries/1/2/merkle:sha-256"));
    }

    @Test
    public void getRecordByKeyRedirect() throws Exception {
        Response response = register.getRequest(address, "/record/elvis", WILDCARD);
        assertThat(response.getStatus(), equalTo(301));
        assertThat(response.getLocation().getPath(), equalTo("/records/elvis"));
    }

    @Test
    public void getRecordByKeyRedirectRetainsFormat() throws Exception {
        Response response = register.getRequest(address, "/record/elvis.json", WILDCARD);
        assertThat(response.getStatus(), equalTo(301));
        assertThat(response.getLocation().getPath(), equalTo("/records/elvis.json"));
    }

    @Test
    public void getRecordEntriesRedirect() throws Exception {
        Response response = register.getRequest(address, "/record/elvis/entries", WILDCARD);
        assertThat(response.getStatus(), equalTo(301));
        assertThat(response.getLocation().getPath(), equalTo("/records/elvis/entries"));
    }

}