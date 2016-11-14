package uk.gov.register.resources;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.register.configuration.RegisterFieldsConfiguration;
import uk.gov.register.core.RegisterData;
import uk.gov.register.core.RegisterMetadata;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.Method;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SearchResourceTest {
    @Mock
    private HttpServletResponse servletResponse;

    RequestContext requestContext;
    SearchResource resource;
    RegisterData registerData;
    RegisterFieldsConfiguration registerFieldsConfiguration;

    @Before
    public void setUp() throws Exception {
        requestContext = new RequestContext(){
            @Override
            public HttpServletResponse getHttpServletResponse() {
                return servletResponse;
            }
        };
        RegisterMetadata registerMetadata = mock(RegisterMetadata.class);
        registerData = mock(RegisterData.class);
        when(registerData.getRegister()).thenReturn(registerMetadata);
        registerFieldsConfiguration = mock(RegisterFieldsConfiguration.class);
    }

    @Test
    public void findSupportsTurtleHtmlAndJson() throws Exception {
        Method searchMethod = SearchResource.class.getDeclaredMethod("find", String.class, String.class);
        List<String> declaredMediaTypes = asList(searchMethod.getDeclaredAnnotation(Produces.class).value());
        assertThat(declaredMediaTypes,
                hasItems(ExtraMediaType.TEXT_HTML,
                        MediaType.APPLICATION_JSON,
                        ExtraMediaType.TEXT_TTL));
    }

    @Test(expected = NotFoundException.class)
    public void find_doesNotRedirect_whenKeyDoesNotExistAsFieldInRegister() throws Exception {
        resource = new SearchResource(requestContext, () -> "school", registerFieldsConfiguration);
        resource.find("country-name", "United Kingdom");
    }

    @Test
    public void find_returns301_whenKeyExistsAsFieldInRegister() throws Exception {
        when(registerFieldsConfiguration.containsField("country-name")).thenReturn(true);
        resource = new SearchResource(requestContext, () -> "school", registerFieldsConfiguration);

        Response r = (Response) resource.find("country-name", "United Kingdom");
        assertThat(r.getStatus(), equalTo(301));
    }
}