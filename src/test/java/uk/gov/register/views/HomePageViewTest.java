package uk.gov.register.views;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.register.configuration.HomepageContent;
import uk.gov.register.core.*;
import uk.gov.register.resources.RequestContext;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.text.IsEmptyString.isEmptyString;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class HomePageViewTest {
    private final RegisterResolver registerResolver = registerId -> URI.create("http://" + registerId + ".test.register.gov.uk");

    @Mock
    RequestContext mockRequestContext;

    HomepageContent homepageContent = new HomepageContent();

    @Test
    public void getLastUpdatedTime_formatsTheLocalDateTimeToUKDateTimeFormat() {
        final Instant instant = LocalDateTime.of(2015, 9, 11, 13, 17, 59, 543).toInstant(ZoneOffset.UTC);
        final RegisterReadOnly register = mock(RegisterReadOnly.class);
        final HomePageView homePageView = new HomePageView(null, null, mockRequestContext, 1, Optional.of(instant), homepageContent, registerResolver, register);

        assertThat(homePageView.getLastUpdatedTime(), equalTo("11 September 2015"));
    }

    @Test
    public void getLastUpdatedTime_returnsEmptyStringIfLastUpdatedTimeNotPresent() {
        final RegisterReadOnly register = mock(RegisterReadOnly.class);
        final HomePageView homePageView = new HomePageView(null, null, mockRequestContext, 1, Optional.empty(), homepageContent, registerResolver, register);

        assertThat(homePageView.getLastUpdatedTime(), isEmptyString());
    }
}
