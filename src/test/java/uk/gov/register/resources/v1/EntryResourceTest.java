package uk.gov.register.resources.v1;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.register.resources.v2.EntryResource;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.lang.reflect.Method;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;

@RunWith(MockitoJUnitRunner.class)
public class EntryResourceTest {

    @Test
    public void findByEntryNumberHtmlSupportsHtml() throws Exception {
        Method findBySerialMethod = EntryResource.class.getDeclaredMethod("findByEntryNumberHtml", int.class);
        List<String> declaredMediaTypes = asList(findBySerialMethod.getDeclaredAnnotation(Produces.class).value());
        assertThat(declaredMediaTypes,
                hasItems(ExtraMediaType.TEXT_HTML));
    }

    @Test
    public void findByEntryNumberSupportsJson() throws Exception {
        Method findBySerialMethod = EntryResource.class.getDeclaredMethod("findByEntryNumber", int.class);
        List<String> declaredMediaTypes = asList(findBySerialMethod.getDeclaredAnnotation(Produces.class).value());
        assertThat(declaredMediaTypes,
                hasItems(MediaType.APPLICATION_JSON));
    }
}
