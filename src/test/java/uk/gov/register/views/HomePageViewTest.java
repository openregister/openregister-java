package uk.gov.register.views;

import org.hamcrest.collection.IsIterableContainingInOrder;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.text.IsEmptyString.isEmptyString;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HomePageViewTest {
    private final RegisterResolver registerResolver = registerId -> URI.create("http://" + registerId + ".test.register.gov.uk");

    @Mock
    RequestContext mockRequestContext;
    @Mock
    private Field field;

    HomepageContent homepageContent = new HomepageContent(emptyList());

    @Test
    public void getIndexes_shouldGetIndexesIfAvailable() {
        final RegisterReadOnly register = mock(RegisterReadOnly.class);
        HomepageContent homepageContent = new HomepageContent(emptyList());
        HomePageView homePageView = new HomePageView(null, null, mockRequestContext, 1, Optional.empty(), homepageContent, registerResolver, register);

        assertThat(homePageView.getHomepageContent().getIndexes(), is(emptyList()));

        final List<String> indexes = Arrays.asList("current-countries", "local-authority-by-type");
        homepageContent = new HomepageContent(indexes);
        homePageView = new HomePageView(null, null, mockRequestContext, 1, Optional.empty(), homepageContent, registerResolver, register);

        assertThat(homePageView.getHomepageContent().getIndexes(), IsIterableContainingInOrder.contains("current-countries", "local-authority-by-type"));
    }

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
