package uk.gov.register.functional;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.register.functional.app.RegisterRule;
import uk.gov.register.functional.app.RsfRegisterDefinition;
import uk.gov.register.functional.app.TestRegister;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.register.functional.app.TestRegister.address;
import static uk.gov.register.functional.app.TestRegister.postcode;
import static uk.gov.register.views.representations.ExtraMediaType.APPLICATION_RSF_TYPE;

public class AuthenticationTest {
    @ClassRule
    public static final RegisterRule register = new RegisterRule();

    @Before
    public void setup() {
        register.loadRsfV1(TestRegister.address, RsfRegisterDefinition.ADDRESS_FIELDS + RsfRegisterDefinition.ADDRESS_REGISTER);
        register.loadRsfV1(TestRegister.postcode, RsfRegisterDefinition.POSTCODE_REGISTER);
    }
    
    @Test
    public void correctCredentials_shouldBeAllowed() {
        String addressRsf = "add-item\t{\"address\":\"1234\"}\n" +
                "append-entry\tuser\t1234\t2018-07-26T16:13:37Z\tsha-256:fdae0299e49857bb0165d5406ee1eb8f3a4465891d01a3100964ced1b80fc63f";

        String postcodeRsf = "add-item\t{\"postcode\":\"1234\"}\n" +
                "append-entry\tuser\t1234\t2018-07-26T16:13:37Z\tsha-256:a2efed1ddf15c08f491b5e8f5ea2550081074018f87530924fcee03eee0a644f";

        Response addressResponse = register.target(address).register(address.httpAuthFeature())
                .path("/load-rsf").request()
                .post(Entity.entity(addressRsf, APPLICATION_RSF_TYPE));
        assertThat(addressResponse.getStatus(), is(200));

        Response postcodeResponse = register.target(postcode).register(postcode.httpAuthFeature())
                .path("/load-rsf").request()
                .post(Entity.entity(postcodeRsf, APPLICATION_RSF_TYPE));
        assertThat(postcodeResponse.getStatus(), is(200));
    }

    @Test
    public void incorrectCredentials_shouldBeRejected() {
        String addressRsf = "add-item\t{\"address\":\"1234\"}\n" +
                "append-entry\tuser\t1234\t2018-07-26T16:13:37Z\tsha-256:fdae0299e49857bb0165d5406ee1eb8f3a4465891d01a3100964ced1b80fc63f";

        String postcodeRsf = "add-item\t{\"postcode\":\"1234\"}\n" +
                "append-entry\tuser\t1234\t2018-07-26T16:13:37Z\tsha-256:a2efed1ddf15c08f491b5e8f5ea2550081074018f87530924fcee03eee0a644f";

        Response addressWithPostcodeCredsResponse = register.target(address).register(postcode.httpAuthFeature())
                .path("/load-rsf").request()
                .post(Entity.entity(addressRsf, APPLICATION_RSF_TYPE));
        assertThat(addressWithPostcodeCredsResponse.getStatus(), is(401));

        Response postcodeWithAddressCredsResponse = register.target(postcode).register(address.httpAuthFeature())
                .path("/load-rsf").request()
                .post(Entity.entity(postcodeRsf, APPLICATION_RSF_TYPE));
        assertThat(postcodeWithAddressCredsResponse.getStatus(), is(401));
    }
}
