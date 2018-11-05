package uk.gov.register.resources.v2;

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
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class EntryResourceTest {
    @Test
    public void findByEntryNumberAvailableInV2() throws Exception {
        Method findBySerialMethod = EntryResource.class.getDeclaredMethod("findByEntryNumber", int.class);
        List<String> declaredMediaTypes = asList(findBySerialMethod.getDeclaredAnnotation(Produces.class).value());

        assertThat(declaredMediaTypes,
                hasItems(MediaType.APPLICATION_JSON,
                        ExtraMediaType.TEXT_CSV));
    }

    @Test
    public void v2DropsSupportForContentTypes() throws Exception {
        Method findBySerialMethod = EntryResource.class.getDeclaredMethod("findByEntryNumber", int.class);
        List<String> declaredMediaTypes = asList(findBySerialMethod.getDeclaredAnnotation(Produces.class).value());

        assertThat(declaredMediaTypes, not(hasItems(ExtraMediaType.TEXT_YAML)));
        assertThat(declaredMediaTypes, not(hasItems(ExtraMediaType.APPLICATION_SPREADSHEET)));
        assertThat(declaredMediaTypes, not(hasItems(ExtraMediaType.TEXT_TSV)));
        assertThat(declaredMediaTypes, not(hasItems(ExtraMediaType.TEXT_TTL)));
    }
}
