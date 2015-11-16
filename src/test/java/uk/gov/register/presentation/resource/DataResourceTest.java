package uk.gov.register.presentation.resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.register.presentation.dao.RecentEntryIndexQueryDAO;
import uk.gov.register.presentation.representations.ExtraMediaType;
import uk.gov.register.presentation.view.ViewFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.mockito.Mockito.when;

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

    DataResource dataResource;

    @Before
    public void setUp() throws Exception {
        dataResource = new DataResource(viewFactory, requestContext, queryDAO);
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

    @Test
    public void currentWithRepresentation_permanentlyRedirectsToRecordsWithSameRepresentation() {

        when(requestContext.requestURI()).thenReturn("http://abc/current.json");

        Response response = dataResource.current(Optional.empty(), Optional.empty());

        assertThat(response.getStatus(), equalTo(301));
        assertThat(response.getHeaderString("Location"), equalTo("http://abc/records.json"));
    }

    @Test
    public void currentWithoutRepresentation_permanentlyRedirectsToRecordsWithoputRepresentation() {
        when(requestContext.requestURI()).thenReturn("http://abc/current");

        Response response = dataResource.current(Optional.empty(), Optional.empty());

        assertThat(response.getStatus(), equalTo(301));
        assertThat(response.getHeaderString("Location"), equalTo("http://abc/records"));
    }

    @Test
    public void feedWithRepresentation_permanentlyRedirectsToEntriesWithSameRepresentation() {
        when(requestContext.requestURI()).thenReturn("http://abc/entries.json");

        Response response = dataResource.feed(Optional.empty(), Optional.empty());

        assertThat(response.getStatus(), equalTo(301));
        assertThat(response.getHeaderString("Location"), equalTo("http://abc/entries.json"));
    }

    @Test
    public void feedWithoutRepresentation_permanentlyRedirectsToEntiresWithoputRepresentation() {
        when(requestContext.requestURI()).thenReturn("http://abc/feed");

        Response response = dataResource.feed(Optional.empty(), Optional.empty());

        assertThat(response.getStatus(), equalTo(301));
        assertThat(response.getHeaderString("Location"), equalTo("http://abc/entries"));
    }
}
