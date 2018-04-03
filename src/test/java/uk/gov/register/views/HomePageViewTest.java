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
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class HomePageViewTest {
    private final RegisterResolver registerResolver = registerId -> URI.create("http://" + registerId + ".test.register.gov.uk");

    @Mock
    RequestContext mockRequestContext;
    @Mock
    private Field field;

    @Test
    public void getIndexes_shouldGetIndexesIfAvailable() {
        final RegisterReadOnly register = mock(RegisterReadOnly.class);
        HomepageContent homepageContent = new HomepageContent(emptyList());
        HomePageView homePageView = new HomePageView(null, null, mockRequestContext, homepageContent, registerResolver, register);

        assertThat(homePageView.getHomepageContent().getIndexes(), is(emptyList()));

        final List<String> indexes = Arrays.asList("current-countries", "local-authority-by-type");
        homepageContent = new HomepageContent(indexes);
        homePageView = new HomePageView(null, null, mockRequestContext, homepageContent, registerResolver, register);

        assertThat(homePageView.getHomepageContent().getIndexes(), IsIterableContainingInOrder.contains("current-countries", "local-authority-by-type"));
    }
}
