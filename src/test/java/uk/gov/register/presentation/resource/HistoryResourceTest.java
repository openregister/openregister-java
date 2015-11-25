package uk.gov.register.presentation.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.register.presentation.DbContent;
import uk.gov.register.presentation.DbEntry;
import uk.gov.register.presentation.Version;
import uk.gov.register.presentation.config.RegistersConfiguration;
import uk.gov.register.presentation.dao.RecentEntryIndexQueryDAO;
import uk.gov.register.presentation.view.ViewFactory;

import javax.ws.rs.NotFoundException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HistoryResourceTest {

    private HistoryResource resource;
    @Mock
    private RecentEntryIndexQueryDAO queryDAO;
    @Mock
    private ViewFactory viewFactory;
    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws Exception {
        resource = new HistoryResource(new RequestContext(new RegistersConfiguration()) {
            @Override
            public String getRegisterPrimaryKey() {
                return "school";
            }
        }, queryDAO, viewFactory);
        objectMapper = Jackson.newObjectMapper();
    }

    @Test
    public void history_throwsNotFoundExceptionForNonPrimaryKeyRequests() throws Exception {
        try {
            resource.history("someOtherKey", "value");
            fail("Must fail");
        } catch (NotFoundException e) {
            //success
        }
    }

    @Test
    @SuppressWarnings("unchecked, limitations of ArgumentCaptor with generics")
    public void history_returnsVersionsFromDatabase() throws Exception {
        when(queryDAO.findAllEntriesByKeyValue("school", "1234")).thenReturn(Arrays.asList(
                new DbEntry(3, new DbContent("hash1", objectMapper.readTree("{\"school\":\"1234\",\"head-teacher\":\"John Smith\"}"))),
                new DbEntry(17, new DbContent("hash2", objectMapper.readTree("{\"school\":\"1234\",\"head-teacher\":\"Caroline Atkins\"}")))
        ));
        ArgumentCaptor<List> versionsCaptor = ArgumentCaptor.forClass(List.class);

        resource.history("school", "1234");

        verify(viewFactory).listVersionView(versionsCaptor.capture());

        List<Version> versions = versionsCaptor.getValue();

        assertThat(versions.get(0).hash, equalTo("hash1"));
        assertThat(versions.get(0).serialNumber, equalTo(3));
        assertThat(versions.get(1).hash, equalTo("hash2"));
        assertThat(versions.get(1).serialNumber, equalTo(17));
    }
}
