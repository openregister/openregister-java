package uk.gov.register.resources;

import org.junit.Test;
import uk.gov.register.db.EntryQueryDAO;
import uk.gov.register.db.RecordQueryDAO;
import uk.gov.register.views.HomePageView;
import uk.gov.register.views.ViewFactory;

import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Optional;

import static org.mockito.Mockito.*;

public class HomePageResourceTest {

    @Test
    public void shouldReturnPageViewWithValidValues() throws NoSuchAlgorithmException {
        int totalRecords = 5;
        int totalEntries = 6;
        Optional<Instant> lastUpdated = Optional.of(Instant.ofEpochMilli(1459241964336L));
        HomePageView homePageView = mock(HomePageView.class);

        RecordQueryDAO recordDAOMock = mock(RecordQueryDAO.class);
        EntryQueryDAO entryDAOMock = mock(EntryQueryDAO.class);
        ViewFactory viewFactoryMock = mock(ViewFactory.class);

        when(recordDAOMock.getTotalRecords()).thenReturn(totalRecords);
        when(entryDAOMock.getTotalEntries()).thenReturn(totalEntries);
        when(entryDAOMock.getLastUpdatedTime()).thenReturn(lastUpdated);
        when(viewFactoryMock.homePageView(totalRecords, totalEntries, lastUpdated)).thenReturn(homePageView);

        HomePageResource homePageResource = new HomePageResource(viewFactoryMock, recordDAOMock, entryDAOMock);
        homePageResource.home();

        verify(recordDAOMock, times(1)).getTotalRecords();
        verify(entryDAOMock, times(1)).getTotalEntries();
        verify(viewFactoryMock, times(1)).homePageView(totalRecords, totalEntries, lastUpdated);
    }
}

