package uk.gov.register.resources;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
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
    public void findByEntryNumberSupportsHtmlAndJson() throws Exception {
        Method findBySerialMethod = EntryResource.class.getDeclaredMethod("findByEntryNumber", int.class);
        List<String> declaredMediaTypes = asList(findBySerialMethod.getDeclaredAnnotation(Produces.class).value());
        assertThat(declaredMediaTypes,
                hasItems(ExtraMediaType.TEXT_HTML,
                        MediaType.APPLICATION_JSON));
    }
}
