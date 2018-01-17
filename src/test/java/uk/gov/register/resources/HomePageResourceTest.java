package uk.gov.register.resources;

import org.junit.Before;
import org.junit.Test;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.views.HomePageView;
import uk.gov.register.views.ViewFactory;

import java.time.Instant;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

public class HomePageResourceTest {

    private RegisterReadOnly registerMock;
    private ViewFactory viewFactoryMock;

    @Before
    public void beforeEach() {
        registerMock = mock(RegisterReadOnly.class);
        viewFactoryMock = mock(ViewFactory.class);
    }

    @Test
    public void shouldReturnPageViewWithValidValues() {
        int totalRecords = 5;
        int totalEntries = 6;
        Optional<Instant> lastUpdated = Optional.of(Instant.ofEpochMilli(1459241964336L));
        Optional<String> custodianName = Optional.of("John Smith");
        HomePageView homePageView = mock(HomePageView.class);

        when(registerMock.getTotalRecords()).thenReturn(totalRecords);
        when(registerMock.getTotalEntries()).thenReturn(totalEntries);
        when(registerMock.getLastUpdatedTime()).thenReturn(lastUpdated);
        when(registerMock.getCustodianName()).thenReturn(custodianName);
        when(viewFactoryMock.homePageView(totalRecords, totalEntries, lastUpdated, custodianName)).thenReturn(homePageView);

        HomePageResource homePageResource = new HomePageResource(registerMock, viewFactoryMock);
        homePageResource.home();

        verify(registerMock, times(1)).getTotalRecords();
        verify(registerMock, times(1)).getTotalEntries();
        verify(registerMock, times(1)).getLastUpdatedTime();
        verify(registerMock, times(1)).getCustodianName();
        verify(viewFactoryMock, times(1)).homePageView(totalRecords, totalEntries, lastUpdated, custodianName);
    }
}
