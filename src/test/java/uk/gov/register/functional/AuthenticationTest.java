package uk.gov.register.functional;

import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.register.functional.app.RegisterRule;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.register.functional.app.TestRegister.address;
import static uk.gov.register.functional.app.TestRegister.postcode;

public class AuthenticationTest {
    @ClassRule
    public static final RegisterRule register = new RegisterRule();
    
    @Test
    public void correctCredentials_shouldBeAllowed() throws Exception {
        Response addressResponse = register.target(address).register(address.httpAuthFeature())
                .path("/load").request()
                .post(Entity.json("{\"address\":\"1234\"}"));
        assertThat(addressResponse.getStatus(), is(204));

        Response postcodeResponse = register.target(postcode).register(postcode.httpAuthFeature())
                .path("/load").request()
                .post(Entity.json("{\"postcode\":\"12345\"}"));
        assertThat(postcodeResponse.getStatus(), is(204));
    }

    @Test
    public void incorrectCredentials_shouldBeRejected() throws Exception {
        Response addressWithPostcodeCredsResponse = register.target(address).register(postcode.httpAuthFeature())
                .path("/load").request()
                .post(Entity.json("{\"address\":\"1234\"}"));
        assertThat(addressWithPostcodeCredsResponse.getStatus(), is(401));

        Response postcodeWithAddressCredsResponse = register.target(postcode).register(address.httpAuthFeature())
                .path("/load").request()
                .post(Entity.json("{\"postcode\":\"1234\"}"));
        assertThat(postcodeWithAddressCredsResponse.getStatus(), is(401));
    }
}
