package uk.gov.register.presentation.resource;

import io.dropwizard.jackson.Jackson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.register.presentation.DbContent;
import uk.gov.register.presentation.DbEntry;
import uk.gov.register.presentation.config.RegistersConfiguration;
import uk.gov.register.presentation.dao.RecentEntryIndexQueryDAO;
import uk.gov.register.presentation.representations.ExtraMediaType;
import uk.gov.register.presentation.view.SingleEntryView;
import uk.gov.register.presentation.view.ViewFactory;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SearchResourceTest {
    @Mock
    RecentEntryIndexQueryDAO queryDAO;
    @Mock
    private ViewFactory viewFactory;
    @Mock
    private HttpServletResponse servletResponse;

    RequestContext requestContext;
    SearchResource resource;

    @Before
    public void setUp() throws Exception {
        requestContext = new RequestContext(new RegistersConfiguration(Optional.empty()), () -> ""){
            @Override
            public HttpServletResponse getHttpServletResponse() {
                return servletResponse;
            }

            @Override
            public String getRegisterPrimaryKey() {
                return "school";
            }
        };
        resource = new SearchResource(viewFactory, requestContext, queryDAO);
    }

    @Test
    public void findByItemHash_throwsNotFoundWhenHashIsNotFound() {
        when(queryDAO.findEntryByHash("123")).thenReturn(Optional.<DbEntry>empty());
        try {
            resource.findByItemHash("123");
            fail("Must fail");
        } catch (NotFoundException e) {
            //success
        }
    }

    @Test
    public void findBySerial_findsEntryFromDb() throws Exception {
        DbEntry abcd = new DbEntry(52, new DbContent("abcd", Jackson.newObjectMapper().readTree("{\"school\":\"9001\",\"address\":\"1234\"}")), "leaf_input");
        when(queryDAO.findEntryBySerialNumber(52)).thenReturn(Optional.of(abcd));
        SingleEntryView expected = mock(SingleEntryView.class);
        when(viewFactory.getSingleEntryView(abcd)).thenReturn(expected);

        SingleEntryView result = resource.findBySerial("52");

        assertThat(result, equalTo(expected));
    }

    @Test
    public void findBySerial_setsHistoryLinkHeader() throws Exception {
        DbEntry abcd = new DbEntry(52, new DbContent("abcd", Jackson.newObjectMapper().readTree("{\"school\":\"9001\",\"address\":\"1234\"}")), "leaf_input");
        when(queryDAO.findEntryBySerialNumber(52)).thenReturn(Optional.of(abcd));
        SingleEntryView singleEntryView = mock(SingleEntryView.class);
        when(viewFactory.getSingleEntryView(abcd)).thenReturn(singleEntryView);
        when(singleEntryView.getVersionHistoryLink()).thenReturn("/school/9001/history");

        resource.findBySerial("52");

        verify(servletResponse).setHeader("Link", "</school/9001/history>;rel=\"version-history\"");
    }

    @Test
    public void findBySerial_reportsNotFoundCorrectly() throws Exception {
        when(queryDAO.findEntryBySerialNumber(52)).thenReturn(Optional.empty());

        try {
            resource.findBySerial("52");
            fail("expected NotFoundException");
        } catch (NotFoundException e) {
            // success
        }
    }

    @Test
    public void findBySerial_reportsBadSerialNumberAsNotFound() throws Exception {
        try {
            resource.findBySerial("abcd");
            fail("expected NotFoundException");
        } catch (NotFoundException e) {
            // success
        }
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

    @Test
    public void findByItemHashSupportsTurtleHtmlAndJson() throws Exception {
        Method findByItemHashMethod = SearchResource.class.getDeclaredMethod("findByItemHash", String.class);
        List<String> declaredMediaTypes = asList(findByItemHashMethod.getDeclaredAnnotation(Produces.class).value());
        assertThat(declaredMediaTypes,
                hasItems(ExtraMediaType.TEXT_HTML,
                        MediaType.APPLICATION_JSON,
                        ExtraMediaType.TEXT_TTL));
    }

    @Test
    public void findBySerialSupportsTurtleHtmlAndJson() throws Exception {
        Method findBySerialMethod = SearchResource.class.getDeclaredMethod("findBySerial", String.class);
        List<String> declaredMediaTypes = asList(findBySerialMethod.getDeclaredAnnotation(Produces.class).value());
        assertThat(declaredMediaTypes,
                hasItems(ExtraMediaType.TEXT_HTML,
                        MediaType.APPLICATION_JSON,
                        ExtraMediaType.TEXT_TTL));
    }
}
