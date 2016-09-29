package uk.gov.register.resources;

import org.junit.Test;
import uk.gov.register.core.RegisterReadOnly;
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

        RegisterReadOnly registerMock = mock(RegisterReadOnly.class);
        ViewFactory viewFactoryMock = mock(ViewFactory.class);

        when(registerMock.getTotalRecords()).thenReturn(totalRecords);
        when(registerMock.getTotalEntries()).thenReturn(totalEntries);
        when(registerMock.getLastUpdatedTime()).thenReturn(lastUpdated);
        when(viewFactoryMock.homePageView(totalRecords, totalEntries, lastUpdated)).thenReturn(homePageView);

        HomePageResource homePageResource = new HomePageResource(registerMock, viewFactoryMock);
        homePageResource.home();

        verify(registerMock, times(1)).getTotalRecords();
        verify(registerMock, times(1)).getTotalEntries();
        verify(viewFactoryMock, times(1)).homePageView(totalRecords, totalEntries, lastUpdated);
    }
}

