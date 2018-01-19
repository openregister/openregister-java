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

    HomepageContent homepageContent = new HomepageContent(Optional.empty(), emptyList(), emptyList());

    @Test
    public void getRegisterText_rendersRegisterTextAsMarkdown() throws Exception {
        final String registerText = "foo *bar* [baz](/quux)";

        final RegisterMetadata registerMetadata = new RegisterMetadata(new RegisterName("widget"), new ArrayList<>(), null, null, registerText, "alpha");
        final RegisterReadOnly register = mock(RegisterReadOnly.class);
        when(register.getRegisterName()).thenReturn(new RegisterName("widget"));
        when(register.getRegisterMetadata()).thenReturn(registerMetadata);

        final HomePageView homePageView = new HomePageView(null, null, mockRequestContext, 1, 2, null, Optional.empty(), homepageContent, registerResolver, Arrays.asList(field), registerLinkService, register);

        final String result = homePageView.getRegisterText();

        assertThat(result, equalTo("<p>foo <em>bar</em> <a href=\"/quux\">baz</a></p>\n"));
    }

    @Test
    public void getLastUpdatedTime_formatsTheLocalDateTimeToUKDateTimeFormat() {
        final Instant instant = LocalDateTime.of(2015, 9, 11, 13, 17, 59, 543).toInstant(ZoneOffset.UTC);
        final RegisterReadOnly register = mock(RegisterReadOnly.class);
        final HomePageView homePageView = new HomePageView(null, null, mockRequestContext, 1, 2, Optional.of(instant), Optional.empty(), homepageContent, registerResolver, Arrays.asList(field), registerLinkService, register);

        assertThat(homePageView.getLastUpdatedTime(), equalTo("11 September 2015"));
    }

    @Test
    public void getLastUpdatedTime_returnsEmptyStringIfLastUpdatedTimeNotPresent() {
        final RegisterReadOnly register = mock(RegisterReadOnly.class);
        final HomePageView homePageView = new HomePageView(null, null, mockRequestContext, 1, 2, Optional.empty(), Optional.empty(), homepageContent, registerResolver, Arrays.asList(field), registerLinkService, register);

        assertThat(homePageView.getLastUpdatedTime(), isEmptyString());
    }

    @Test
    public void getLinkToRegisterRegister_returnsTheLinkOfRegisterRegister() {
        final Instant instant = LocalDateTime.of(2015, 9, 11, 13, 17, 59, 543).toInstant(ZoneOffset.UTC);
        final RegisterReadOnly register = mock(RegisterReadOnly.class);
        when(register.getRegisterName()).thenReturn(new RegisterName("school"));
        final HomePageView homePageView = new HomePageView(null, null, mockRequestContext, 1, 2, Optional.of(instant), Optional.empty(), homepageContent, registerResolver, Arrays.asList(field), registerLinkService, register);

        assertThat(homePageView.getLinkToRegisterRegister(), equalTo(URI.create("http://register.test.register.gov.uk/record/school")));
    }

    @Test
    public void shouldGetHistoryPageIfAvailable() {
        final RegisterReadOnly register = mock(RegisterReadOnly.class);

        HomepageContent homepageContent = new HomepageContent(Optional.empty(), emptyList(), emptyList());
        HomePageView homePageView = new HomePageView(null, null, mockRequestContext, 1, 2, Optional.empty(), Optional.empty(), homepageContent, registerResolver, Arrays.asList(field), registerLinkService, register);

        assertThat(homePageView.getHomepageContent().getRegisterHistoryPageUrl().isPresent(), is(false));

        final String historyUrl = "http://register-history.openregister.org";
        homepageContent = new HomepageContent(Optional.of(historyUrl), emptyList(), emptyList());
        homePageView = new HomePageView(null, null, mockRequestContext, 1, 2, Optional.empty(), Optional.empty(), homepageContent, registerResolver, Arrays.asList(field), registerLinkService, register);

        assertThat(homePageView.getHomepageContent().getRegisterHistoryPageUrl().isPresent(), is(true));
        assertThat(homePageView.getHomepageContent().getRegisterHistoryPageUrl().get(), is(historyUrl));
    }

    @Test
    public void shouldGetCustodianNameIfAvailable() {
        final RegisterReadOnly register = mock(RegisterReadOnly.class);
        HomepageContent homepageContent = new HomepageContent(Optional.empty(), emptyList(), emptyList());
        HomePageView homePageView = new HomePageView(null, null, mockRequestContext, 1, 2, Optional.empty(), Optional.empty(), homepageContent, registerResolver, Arrays.asList(field), registerLinkService, register);

        assertThat(homePageView.getCustodianName().isPresent(), is(false));

        final String custodianName = "John Smith";
        homepageContent = new HomepageContent(Optional.of(custodianName), emptyList(), emptyList());
        homePageView = new HomePageView(null, null, mockRequestContext, 1, 2, Optional.empty(), Optional.of(custodianName), homepageContent, registerResolver, Arrays.asList(field), registerLinkService, register);

        assertThat(homePageView.getCustodianName().isPresent(), is(true));
        assertThat(homePageView.getCustodianName().get(), is(custodianName));
    }

    @Test
    public void getFields_shouldGetFields() {
        final Field height = new Field("height", "", null, null, "");
        final Field width = new Field("width", "", null, null, "");
        final Field title = new Field("title", "", null, null, "");

        final List<Field> fields = Arrays.asList(height, width, title);

        final RegisterReadOnly register = mock(RegisterReadOnly.class);
        when(register.getRegisterName()).thenReturn(new RegisterName("widget"));
        final HomePageView homePageView = new HomePageView(null, null, mockRequestContext, 1, 2, null, Optional.empty(), homepageContent, registerResolver, fields, registerLinkService, register);

        final List<Field> actualFields = Lists.newArrayList(homePageView.getFields());

        assertThat(actualFields, IsIterableContainingInOrder.contains(height, width, title));
    }

    @Test
    public void getRegisterLinks_shouldGetRegisterLinks() {
        final RegisterName registerName = new RegisterName("premises");
        final RegisterReadOnly register = mock(RegisterReadOnly.class);
        when(register.getRegisterName()).thenReturn(registerName);
        when(registerLinkService.getRegisterLinks(registerName)).thenReturn(new RegisterLinks(new ArrayList<>(), new ArrayList<>()));

        final HomepageContent homepageContent = new HomepageContent(Optional.empty(), emptyList(), emptyList());
        HomePageView homePageView = new HomePageView(null, null, mockRequestContext, 1, 2, Optional.empty(), Optional.empty(), homepageContent, registerResolver, Arrays.asList(field), registerLinkService, register);

        assertThat(homePageView.getRegisterLinks().getRegistersLinkedFrom(), is(empty()));
        assertThat(homePageView.getRegisterLinks().getRegistersLinkedTo(), is(empty()));

        final List<String> expectedLinkedFromRegisters = Arrays.asList("business");
        final List<String> expectedLinkedToRegisters = Arrays.asList("company", "industry");

        when(registerLinkService.getRegisterLinks(registerName)).thenReturn(new RegisterLinks(expectedLinkedFromRegisters, expectedLinkedToRegisters));
        homePageView = new HomePageView(null, null, mockRequestContext, 1, 2, Optional.empty(), Optional.empty(), homepageContent, registerResolver, Arrays.asList(field), registerLinkService, register);

        assertThat(homePageView.getRegisterLinks().getRegistersLinkedFrom(), equalTo(expectedLinkedFromRegisters));
        assertThat(homePageView.getRegisterLinks().getRegistersLinkedTo(), equalTo(expectedLinkedToRegisters));
    }

    @Test
    public void getSimilarRegisters_shouldGetSimilarRegistersIfAvailable() {
        final RegisterReadOnly register = mock(RegisterReadOnly.class);
        HomepageContent homepageContent = new HomepageContent(Optional.empty(), emptyList(), emptyList());
        HomePageView homePageView = new HomePageView(null, null, mockRequestContext, 1, 2, Optional.empty(), Optional.empty(), homepageContent, registerResolver, Arrays.asList(field), registerLinkService, register);

        assertThat(homePageView.getHomepageContent().getSimilarRegisters(), is(empty()));

        final List<String> similarRegisters = Arrays.asList("address", "territory");
        homepageContent = new HomepageContent(Optional.empty(), similarRegisters, emptyList());
        homePageView = new HomePageView(null, null, mockRequestContext, 1, 2, Optional.empty(), Optional.empty(), homepageContent, registerResolver, Arrays.asList(field), registerLinkService, register);

        assertThat(homePageView.getHomepageContent().getSimilarRegisters(), IsIterableContainingInOrder.contains("address", "territory"));
    }

    @Test
    public void getIndexes_shouldGetIndexesIfAvailable() {
        final RegisterReadOnly register = mock(RegisterReadOnly.class);
        HomepageContent homepageContent = new HomepageContent(Optional.empty(), emptyList(), emptyList());
        HomePageView homePageView = new HomePageView(null, null, mockRequestContext, 1, 2, Optional.empty(), Optional.empty(), homepageContent, registerResolver, Arrays.asList(field), registerLinkService, register);

        assertThat(homePageView.getHomepageContent().getIndexes(), is(emptyList()));

        final List<String> indexes = Arrays.asList("current-countries", "local-authority-by-type");
        homepageContent = new HomepageContent(Optional.empty(), emptyList(), indexes);
        homePageView = new HomePageView(null, null, mockRequestContext, 1, 2, Optional.empty(), Optional.empty(), homepageContent, registerResolver, Arrays.asList(field), registerLinkService, register);

        assertThat(homePageView.getHomepageContent().getIndexes(), IsIterableContainingInOrder.contains("current-countries", "local-authority-by-type"));
    }
}
