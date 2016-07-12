package uk.gov.register.presentation.resource;

import org.junit.Test;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.lang.reflect.Method;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;

public class RegisterResourceTest {
    @Test
    public void register_supportsJson() throws Exception {
        Method registerMethod = RegisterResource.class.getDeclaredMethod("getRegisterDetail");
        List<String> declaredMediaTypes = asList(registerMethod.getAnnotation(Produces.class).value());
        assertThat(declaredMediaTypes, hasItems(MediaType.APPLICATION_JSON));
    }
}
