package uk.gov.register.presentation.resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.register.presentation.dao.RecentEntryIndexQueryDAO;
import uk.gov.register.presentation.representations.ExtraMediaType;
import uk.gov.register.presentation.view.ViewFactory;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.lang.reflect.Method;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;

@RunWith(MockitoJUnitRunner.class)
public class EntryResourceTest {
    @Mock
    RecentEntryIndexQueryDAO queryDAO;
    @Mock
    private ViewFactory viewFactory;
    @Mock
    private HttpServletResponse servletResponse;

    @Test
    public void findByEntryNumberSupportsHtmlAndJson() throws Exception {
        Method findBySerialMethod = EntryResource.class.getDeclaredMethod("findByEntryNumber", int.class);
        List<String> declaredMediaTypes = asList(findBySerialMethod.getDeclaredAnnotation(Produces.class).value());
        assertThat(declaredMediaTypes,
                hasItems(ExtraMediaType.TEXT_HTML,
                        MediaType.APPLICATION_JSON));
    }
}
