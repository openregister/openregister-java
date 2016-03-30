package uk.gov.register.presentation.resource;

import io.dropwizard.jackson.Jackson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.skife.jdbi.v2.sqlobject.Bind;
import uk.gov.register.presentation.DbContent;
import uk.gov.register.presentation.DbEntry;
import uk.gov.register.presentation.config.RegistersConfiguration;
import uk.gov.register.presentation.dao.Entry;
import uk.gov.register.presentation.dao.EntryDAO;
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
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EntryResourceTest {
    @Mock
    RecentEntryIndexQueryDAO queryDAO;
    @Mock
    private ViewFactory viewFactory;
    @Mock
    private HttpServletResponse servletResponse;

    private EntryDAO entryDAO = new EntryDAO() {
        @Override
        public Optional<Entry> findByEntryNumber(@Bind("entry_number") int entryNumber) {
            throw new RuntimeException(new PSQLException("", new PSQLState("42P01")));
        }

        @Override
        public List<Entry> getAll() {
            return null;
        }
    };

    private EntryResource resource;

    @Before
    public void setUp() throws Exception {
        RequestContext requestContext = new RequestContext(new RegistersConfiguration(Optional.empty()), () -> "") {
            @Override
            public HttpServletResponse getHttpServletResponse() {
                return servletResponse;
            }

            @Override
            public String getRegisterPrimaryKey() {
                return "school";
            }
        };
        resource = new EntryResource(entryDAO, viewFactory, queryDAO, requestContext);
    }

    @Test
    public void findByEntryNumberSupportsHtmlAndJson() throws Exception {
        Method findBySerialMethod = EntryResource.class.getDeclaredMethod("findByEntryNumber", int.class);
        List<String> declaredMediaTypes = asList(findBySerialMethod.getDeclaredAnnotation(Produces.class).value());
        assertThat(declaredMediaTypes,
                hasItems(ExtraMediaType.TEXT_HTML,
                        MediaType.APPLICATION_JSON));
    }


    //Todo: these all tests are for backward compatibility, should be removed once migration is completed
    @Test
    public void findBySerial_findsEntryFromDb() throws Exception {
        DbEntry abcd = new DbEntry(52, new DbContent("abcd", Jackson.newObjectMapper().readTree("{\"school\":\"9001\",\"address\":\"1234\"}")));
        when(queryDAO.findEntryBySerialNumber(52)).thenReturn(Optional.of(abcd));
        SingleEntryView expected = mock(SingleEntryView.class);
        when(viewFactory.getSingleEntryView(abcd)).thenReturn(expected);

        SingleEntryView result = (SingleEntryView) resource.findByEntryNumber(52);

        assertThat(result, equalTo(expected));
    }

    @Test
    public void findBySerial_setsHistoryLinkHeader() throws Exception {
        DbEntry abcd = new DbEntry(52, new DbContent("abcd", Jackson.newObjectMapper().readTree("{\"school\":\"9001\",\"address\":\"1234\"}")));
        when(queryDAO.findEntryBySerialNumber(52)).thenReturn(Optional.of(abcd));
        SingleEntryView singleEntryView = mock(SingleEntryView.class);
        when(viewFactory.getSingleEntryView(abcd)).thenReturn(singleEntryView);
        when(singleEntryView.getVersionHistoryLink()).thenReturn("/school/9001/history");

        resource.findByEntryNumber(52);

        verify(servletResponse).setHeader("Link", "</school/9001/history>;rel=\"version-history\"");
    }

    @Test
    public void findBySerial_reportsNotFoundCorrectly() throws Exception {
        when(queryDAO.findEntryBySerialNumber(52)).thenReturn(Optional.empty());

        try {
            resource.findByEntryNumber(52);
            fail("expected NotFoundException");
        } catch (NotFoundException e) {
            // success
        }
    }
}
