package uk.gov.register.views;

import org.apache.jena.ext.com.google.common.collect.Lists;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.register.configuration.HomepageContent;
import uk.gov.register.core.*;
import uk.gov.register.resources.RequestContext;
import uk.gov.register.service.RegisterLinkService;

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
    private final RegisterResolver registerResolver = registerName -> URI.create("http://" + registerName + ".test.register.gov.uk");

    @Mock
    RequestContext mockRequestContext;
    @Mock
    private Field field;
    @Mock
    RegisterLinkService registerLinkService;

    HomepageContent homepageContent = new HomepageContent(emptyList(), emptyList());

    @Test
    public void getIndexes_shouldGetIndexesIfAvailable() {
        final RegisterReadOnly register = mock(RegisterReadOnly.class);
        HomepageContent homepageContent = new HomepageContent(emptyList(), emptyList());
        HomePageView homePageView = new HomePageView(null, null, mockRequestContext, 1, 2, Optional.empty(), Optional.empty(), homepageContent, registerResolver, Arrays.asList(field), registerLinkService, register);

        assertThat(homePageView.getHomepageContent().getIndexes(), is(emptyList()));

        final List<String> indexes = Arrays.asList("current-countries", "local-authority-by-type");
        homepageContent = new HomepageContent(emptyList(), indexes);
        homePageView = new HomePageView(null, null, mockRequestContext, 1, 2, Optional.empty(), Optional.empty(), homepageContent, registerResolver, Arrays.asList(field), registerLinkService, register);

        assertThat(homePageView.getHomepageContent().getIndexes(), IsIterableContainingInOrder.contains("current-countries", "local-authority-by-type"));
    }
}
