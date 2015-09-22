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
    public void feedSupportsJsonCsvTsv() throws Exception {
        Method feedMethod = DataResource.class.getDeclaredMethod("feed", Optional.class, Optional.class);
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
    public void currentSupportsJsonCsvTsvHtmlAndTurtle() throws Exception {
        Method allMethod = DataResource.class.getDeclaredMethod("current");
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
    public void allWithRepresentation_permanentlyRedirectsToCurrentWithSameRepresentation() {

        when(requestContext.requestURI()).thenReturn("http://abc/all.json");

        Response response = dataResource.all();

        assertThat(response.getStatus(), equalTo(301));
        assertThat(response.getHeaderString("Location"), equalTo("http://abc/current.json"));
    }

    @Test
    public void allWithoutRepresentation_permanentlyRedirectsToCurrentWithoputRepresentation() {
        when(requestContext.requestURI()).thenReturn("http://abc/all");

        Response response = dataResource.all();

        assertThat(response.getStatus(), equalTo(301));
        assertThat(response.getHeaderString("Location"), equalTo("http://abc/current"));
    }

    @Test
    public void latestWithRepresentation_permanentlyRedirectsToFeedWithSameRepresentation() {
        when(requestContext.requestURI()).thenReturn("http://abc/latest.json");

        Response response = dataResource.latest();

        assertThat(response.getStatus(), equalTo(301));
        assertThat(response.getHeaderString("Location"), equalTo("http://abc/feed.json"));
    }

    @Test
    public void latestWithoutRepresentation_permanentlyRedirectsToFeedWithoputRepresentation() {
        when(requestContext.requestURI()).thenReturn("http://abc/latest");

        Response response = dataResource.latest();

        assertThat(response.getStatus(), equalTo(301));
        assertThat(response.getHeaderString("Location"), equalTo("http://abc/feed"));
    }
}
