package uk.gov.register.presentation.resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.register.presentation.dao.RecentEntryIndexQueryDAO;
import uk.gov.register.presentation.dao.SignedTreeHeadQueryDAO;
import uk.gov.register.presentation.representations.ExtraMediaType;
import uk.gov.register.presentation.view.ViewFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;

@SuppressWarnings("unchecked")
@RunWith(MockitoJUnitRunner.class)
public class DataResourceTest {
    @Mock
    HttpServletRequest mockHttpServletRequest;

    @Mock
    RequestContext requestContext;

    @Mock
    RecentEntryIndexQueryDAO queryDAO;

    @Mock
    ViewFactory viewFactory;

    @Mock
    SignedTreeHeadQueryDAO signedTreeHeadQueryDAO;

    DataResource dataResource;

    @Before
    public void setUp() throws Exception {
        dataResource = new DataResource(viewFactory, requestContext, queryDAO, signedTreeHeadQueryDAO);
    }

    @Test
    public void entries_supportsJsonCsvTsv() throws Exception {
        Method feedMethod = DataResource.class.getDeclaredMethod("entries", Optional.class, Optional.class);
        List<String> declaredMediaTypes = asList(feedMethod.getAnnotation(Produces.class).value());
        assertThat(declaredMediaTypes, hasItems(
                MediaType.APPLICATION_JSON,
                ExtraMediaType.TEXT_HTML,
                ExtraMediaType.TEXT_CSV,
                ExtraMediaType.TEXT_TSV,
                ExtraMediaType.TEXT_TTL
        ));
    }

    @Test
    public void records_supportsJsonCsvTsvHtmlAndTurtle() throws Exception {
        Method allMethod = DataResource.class.getDeclaredMethod("records", Optional.class, Optional.class);
        List<String> declaredMediaTypes = asList(allMethod.getAnnotation(Produces.class).value());
        assertThat(declaredMediaTypes, hasItems(
                MediaType.APPLICATION_JSON,
                ExtraMediaType.TEXT_HTML,
                ExtraMediaType.TEXT_CSV,
                ExtraMediaType.TEXT_TSV,
                ExtraMediaType.TEXT_TTL
        ));
    }
}
