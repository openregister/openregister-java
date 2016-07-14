package uk.gov.register.presentation.resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.register.presentation.RegisterData;
import uk.gov.register.presentation.config.RegistersConfiguration;
import uk.gov.register.presentation.representations.ExtraMediaType;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;

@RunWith(MockitoJUnitRunner.class)
public class SearchResourceTest {
    @Mock
    private HttpServletResponse servletResponse;

    RequestContext requestContext;
    SearchResource resource;

    @Before
    public void setUp() throws Exception {
        requestContext = new RequestContext(new RegisterData(Collections.emptyMap()), () -> ""){
            @Override
            public HttpServletResponse getHttpServletResponse() {
                return servletResponse;
            }

            @Override
            public String getRegisterPrimaryKey() {
                return "school";
            }
        };
        resource = new SearchResource(requestContext);
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

}
