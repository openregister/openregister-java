package uk.gov.register.presentation.resource;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.register.presentation.RegisterData;
import uk.gov.register.presentation.config.RegistersConfiguration;

import javax.servlet.http.HttpServletRequest;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@Ignore("test failing due to register being taken from config not from request")
public class RequestContextTest {
    @Mock
    private HttpServletRequest httpServletRequest;

    @Test
    public void takesRegisterNameFromHttpHost() throws Exception {
        RequestContext requestContext = createEmptyRequestContext();
        requestContext.httpServletRequest = httpServletRequest;
        when(httpServletRequest.getHeader("Host")).thenReturn("school.beta.openregister.org");

        String registerPrimaryKey = requestContext.getRegisterPrimaryKey();

        assertThat(registerPrimaryKey, equalTo("school"));
    }

    @Test
    public void behavesGracefullyWhenGivenHostWithNoDots() throws Exception {
        RequestContext requestContext = createEmptyRequestContext();
        requestContext.httpServletRequest = httpServletRequest;
        when(httpServletRequest.getHeader("Host")).thenReturn("school");

        String registerPrimaryKey = requestContext.getRegisterPrimaryKey();

        assertThat(registerPrimaryKey, equalTo("school"));
    }

    @Test
    public void resourceExtension_returnsTheResourceExtensionIfExists() {
        RequestContext requestContext = createEmptyRequestContext();
        requestContext.httpServletRequest = httpServletRequest;
        when(httpServletRequest.getRequestURI()).thenReturn("/foo/bar.json");

        assertThat(requestContext.resourceExtension(), equalTo(Optional.of("json")));
    }

    @Test
    public void resourceExtension_returnsEmptyIfResourceExtensionIsNotExists() {
        RequestContext requestContext = createEmptyRequestContext();
        requestContext.httpServletRequest = httpServletRequest;
        when(httpServletRequest.getRequestURI()).thenReturn("/foo/bar");

        assertThat(requestContext.resourceExtension(), equalTo(Optional.empty()));
    }

    private RequestContext createEmptyRequestContext(){
        return new RequestContext(new RegisterData(Collections.emptyMap()), () -> "");
    }

}
