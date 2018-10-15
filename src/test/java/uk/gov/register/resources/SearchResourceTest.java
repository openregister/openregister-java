package uk.gov.register.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import uk.gov.register.configuration.RegisterFieldsConfiguration;
import uk.gov.register.core.*;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SearchResourceTest {
    SearchResource resource;
    RegisterFieldsConfiguration registerFieldsConfiguration;
    RegisterReadOnly register;
    RegisterMetadata registerMetadata;

    @Before
    public void setUp() throws Exception {
        registerFieldsConfiguration = mock(RegisterFieldsConfiguration.class);
        register = mock(RegisterReadOnly.class);
        registerMetadata = mock(RegisterMetadata.class);
        when(registerMetadata.getFields()).thenReturn(Arrays.asList("country-name"));
        when(register.getRegisterMetadata()).thenReturn(registerMetadata);
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

        resource = new SearchResource(new RegisterId("school"), register);
        resource.find("invalid-field-name", "United Kingdom");
    }

    @Test
    public void find_returns301_whenKeyExistsAsFieldInRegister() throws Exception {
        when(registerFieldsConfiguration.containsField("country-name")).thenReturn(true);
        resource = new SearchResource(new RegisterId("school"), register);

        Response r = (Response) resource.find("country-name", "United Kingdom");
        assertThat(r.getStatus(), equalTo(301));
    }

    private Blob getItem(String json) throws IOException {
        return new Blob(new ObjectMapper().readTree(json));
    }
}
