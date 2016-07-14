package uk.gov.register.presentation.resource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.register.presentation.RegisterData;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RequestContextTest {
    @Mock
    private HttpServletRequest httpServletRequest;

    private RequestContext requestContext;

    @Before
    public void beforeEach() throws IOException {
        requestContext = createRequestContext();
    }

    @Test
    public void takesRegisterNameFromRegisterData() throws IOException {
        assertThat(requestContext.getRegisterPrimaryKey(), equalTo("foobar"));
    }

    @Test
    public void resourceExtension_returnsTheResourceExtensionIfExists() {
        when(httpServletRequest.getRequestURI()).thenReturn("/foo/bar.json");

        assertThat(requestContext.resourceExtension(), equalTo(Optional.of("json")));
    }

    @Test
    public void resourceExtension_returnsEmptyIfResourceExtensionIsNotExists() {
        when(httpServletRequest.getRequestURI()).thenReturn("/foo/bar");

        assertThat(requestContext.resourceExtension(), equalTo(Optional.empty()));
    }

    private RequestContext createRequestContext() throws IOException {
        HashMap<String, JsonNode> configData = new ObjectMapper().readValue("{\"register\":\"foobar\"}", HashMap.class);
        RequestContext resultRequestContext = new RequestContext(new RegisterData(configData), () -> "");

        httpServletRequest = Mockito.mock(HttpServletRequest.class);
        resultRequestContext.httpServletRequest = httpServletRequest;
        
        return resultRequestContext;
    }
}
