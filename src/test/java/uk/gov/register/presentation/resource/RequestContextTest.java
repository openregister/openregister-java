package uk.gov.register.presentation.resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.register.presentation.config.RegistersConfiguration;

import javax.servlet.http.HttpServletRequest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RequestContextTest {
    @Mock
    private HttpServletRequest httpServletRequest;

    @Test
    public void takesRegisterNameFromHttpHost() throws Exception {
        RequestContext requestContext = new RequestContext(new RegistersConfiguration());
        requestContext.httpServletRequest = httpServletRequest;
        when(httpServletRequest.getHeader("Host")).thenReturn("school.beta.openregister.org");

        String registerPrimaryKey = requestContext.getRegisterPrimaryKey();

        assertThat(registerPrimaryKey, equalTo("school"));
    }

    @Test
    public void behavesGracefullyWhenGivenHostWithNoDots() throws Exception {
        RequestContext requestContext = new RequestContext(new RegistersConfiguration());
        requestContext.httpServletRequest = httpServletRequest;
        when(httpServletRequest.getHeader("Host")).thenReturn("school");

        String registerPrimaryKey = requestContext.getRegisterPrimaryKey();

        assertThat(registerPrimaryKey, equalTo("school"));
    }
}
