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
        HomePageView homePageView = mock(HomePageView.class);

        when(registerMock.getTotalRecords()).thenReturn(totalRecords);
        when(registerMock.getTotalEntries()).thenReturn(totalEntries);
        when(registerMock.getLastUpdatedTime()).thenReturn(lastUpdated);
        when(viewFactoryMock.homePageView(totalRecords, totalEntries, lastUpdated)).thenReturn(homePageView);

        HomePageResource homePageResource = new HomePageResource(registerMock, viewFactoryMock, () -> Optional.of("trackingId"));
        homePageResource.home();

        verify(registerMock, times(1)).getTotalRecords();
        verify(registerMock, times(1)).getTotalEntries();
        verify(viewFactoryMock, times(1)).homePageView(totalRecords, totalEntries, lastUpdated);
    }

    @Test
    public void shouldRenderAnalyticsCodeIfPresent() throws Exception {
        HomePageResource homePageResource = new HomePageResource(registerMock, viewFactoryMock, () -> Optional.of("codeForTest"));

        String s = homePageResource.analyticsTrackingId();

        assertThat(s, equalTo("var gaTrackingId = \"codeForTest\";\n"));
    }

    @Test
    public void shouldRenderEmptyJsFileIfCodeIsAbsent() throws Exception {
        HomePageResource homePageResource = new HomePageResource(registerMock, viewFactoryMock, () -> Optional.empty());

        String s = homePageResource.analyticsTrackingId();

        assertThat(s, equalTo(""));
    }
}

