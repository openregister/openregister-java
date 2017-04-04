package uk.gov.register.views;

import org.apache.jena.ext.com.google.common.collect.Lists;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.register.configuration.FieldsConfiguration;
import uk.gov.register.configuration.HomepageContent;
import uk.gov.register.core.*;
import uk.gov.register.resources.RequestContext;
import uk.gov.register.service.RegisterLinkService;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

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
    FieldsConfiguration fieldsConfiguration;

    @Mock
    RegisterLinkService registerLinkService;

    HomepageContent homepageContent = new HomepageContent(Optional.empty(), Optional.empty(), emptyList());

    @Test
    public void getRegisterText_rendersRegisterTextAsMarkdown() throws Exception {
        String registerText = "foo *bar* [baz](/quux)";

        RegisterMetadata registerMetadata = new RegisterMetadata(new RegisterName("widget"), new ArrayList<>(), null, null, registerText, "alpha");
        RegisterReadOnly register = mock(RegisterReadOnly.class);
        when(register.getRegisterName()).thenReturn(new RegisterName("widget"));
        when(register.getRegisterMetadata()).thenReturn(registerMetadata);

        HomePageView homePageView = new HomePageView(null, null, mockRequestContext, 1, 2, null, register, homepageContent, () -> Optional.empty(), registerResolver, fieldsConfiguration, registerLinkService);

        String result = homePageView.getRegisterText();

        assertThat(result, equalTo("<p>foo <em>bar</em> <a href=\"/quux\">baz</a></p>\n"));
    }

    @Test
    public void getLastUpdatedTime_formatsTheLocalDateTimeToUKDateTimeFormat() {
        Instant instant = LocalDateTime.of(2015, 9, 11, 13, 17, 59, 543).toInstant(ZoneOffset.UTC);

        RegisterMetadata registerMetadata = mock(RegisterMetadata.class);
        RegisterReadOnly register = mock(RegisterReadOnly.class);
        when(register.getRegisterMetadata()).thenReturn(registerMetadata);

        HomePageView homePageView = new HomePageView(null, null, mockRequestContext, 1, 2, Optional.of(instant), register, homepageContent, () -> Optional.empty(), registerResolver, fieldsConfiguration, registerLinkService);

        assertThat(homePageView.getLastUpdatedTime(), equalTo("11 September 2015"));
    }

    @Test
    public void getLastUpdatedTime_returnsEmptyStringIfLastUpdatedTimeNotPresent() {
        RegisterMetadata registerMetadata = mock(RegisterMetadata.class);
        RegisterReadOnly register = mock(RegisterReadOnly.class);
        when(register.getRegisterMetadata()).thenReturn(registerMetadata);

        HomePageView homePageView = new HomePageView(null, null, mockRequestContext, 1, 2, Optional.empty(), register, homepageContent, () -> Optional.empty(), registerResolver, fieldsConfiguration, registerLinkService);

        assertThat(homePageView.getLastUpdatedTime(), isEmptyString());
    }

    @Test
    public void getLinkToRegisterRegister_returnsTheLinkOfRegisterRegister(){
        Instant instant = LocalDateTime.of(2015, 9, 11, 13, 17, 59, 543).toInstant(ZoneOffset.UTC);

        when(mockRequestContext.getScheme()).thenReturn("https");

        RegisterMetadata registerMetadata = mock(RegisterMetadata.class);

        RegisterReadOnly register = mock(RegisterReadOnly.class);
        when(register.getRegisterName()).thenReturn(new RegisterName("school"));
        when(register.getRegisterMetadata()).thenReturn(registerMetadata);
        HomePageView homePageView = new HomePageView(null, null, mockRequestContext, 1, 2, Optional.of(instant), register, homepageContent, () -> Optional.empty(), registerResolver, fieldsConfiguration, registerLinkService);

        assertThat(homePageView.getLinkToRegisterRegister(), equalTo(URI.create("http://register.test.register.gov.uk/record/school")));
    }

    @Test
    public void shouldGetHistoryPageIfAvailable() {
        RegisterMetadata registerMetadata = mock(RegisterMetadata.class);
        RegisterReadOnly register = mock(RegisterReadOnly.class);
        when(register.getRegisterMetadata()).thenReturn(registerMetadata);

        HomepageContent homepageContent = new HomepageContent(Optional.empty(), Optional.empty(), emptyList());
        HomePageView homePageView = new HomePageView(null, null, mockRequestContext, 1, 2, Optional.empty(), register, homepageContent, () -> Optional.empty(), registerResolver, fieldsConfiguration, registerLinkService);

        assertThat(homePageView.getHomepageContent().getRegisterHistoryPageUrl().isPresent(), is(false));

        String historyUrl = "http://register-history.openregister.org";
        homepageContent = new HomepageContent(Optional.of(historyUrl), Optional.empty(), emptyList());
        homePageView = new HomePageView(null, null, mockRequestContext, 1, 2, Optional.empty(), register, homepageContent, () -> Optional.empty(), registerResolver, fieldsConfiguration, registerLinkService);

        assertThat(homePageView.getHomepageContent().getRegisterHistoryPageUrl().isPresent(), is(true));
        assertThat(homePageView.getHomepageContent().getRegisterHistoryPageUrl().get(), is(historyUrl));
    }

    @Test
    public void shouldGetCustodianNameIfAvailable() {
        RegisterMetadata registerMetadata = mock(RegisterMetadata.class);
        RegisterReadOnly register = mock(RegisterReadOnly.class);
        when(register.getRegisterMetadata()).thenReturn(registerMetadata);

        HomepageContent homepageContent = new HomepageContent(Optional.empty(), Optional.empty(), emptyList());
        HomePageView homePageView = new HomePageView(null, null, mockRequestContext, 1, 2, Optional.empty(), register, homepageContent, () -> Optional.empty(), registerResolver, fieldsConfiguration, registerLinkService);

        assertThat(homePageView.getHomepageContent().getCustodianName().isPresent(), is(false));

        String custodianName = "John Smith";
        homepageContent = new HomepageContent(Optional.empty(), Optional.of(custodianName), emptyList());
        homePageView = new HomePageView(null, null, mockRequestContext, 1, 2, Optional.empty(), register, homepageContent, () -> Optional.empty(), registerResolver, fieldsConfiguration, registerLinkService);

        assertThat(homePageView.getHomepageContent().getCustodianName().isPresent(), is(true));
        assertThat(homePageView.getHomepageContent().getCustodianName().get(), is(custodianName));
    }

    @Test
    public void getFields_shouldGetFields() {
        Field height = new Field(new FieldItem("height", "", null, null, ""));
        Field width = new Field(new FieldItem("width", "", null, null, ""));
        Field title = new Field(new FieldItem("title", "", null, null, ""));

        when(fieldsConfiguration.getField("height")).thenReturn(height);
        when(fieldsConfiguration.getField("width")).thenReturn(width);
        when(fieldsConfiguration.getField("title")).thenReturn(title);

        RegisterMetadata registerMetadata = new RegisterMetadata(new RegisterName("widget"), Arrays.asList("height", "width", "title"), null, null, null, "alpha");
        RegisterReadOnly register = mock(RegisterReadOnly.class);
        when(register.getRegisterName()).thenReturn(new RegisterName("widget"));
        when(register.getRegisterMetadata()).thenReturn(registerMetadata);

        HomePageView homePageView = new HomePageView(null, null, mockRequestContext, 1, 2, null, register, homepageContent, () -> Optional.empty(), registerResolver, fieldsConfiguration, registerLinkService);

        List<Field> actualFields = Lists.newArrayList(homePageView.getFields());

        assertThat(actualFields, IsIterableContainingInOrder.contains(height, width, title));
    }

    @Test
    public void getRegisterLinks_shouldGetRegisterLinks() {
        RegisterName registerName = new RegisterName("premises");
        RegisterMetadata registerMetadata = new RegisterMetadata(registerName, new ArrayList<>(), null, null, null, "alpha");
        RegisterReadOnly register = mock(RegisterReadOnly.class);
        when(register.getRegisterName()).thenReturn(registerName);
        when(register.getRegisterMetadata()).thenReturn(registerMetadata);
        when(registerLinkService.getRegisterLinks(registerName)).thenReturn(new RegisterLinks(new ArrayList<>(), new ArrayList<>()));

        HomepageContent homepageContent = new HomepageContent(Optional.empty(), Optional.empty(), emptyList());
        HomePageView homePageView = new HomePageView(null, null, mockRequestContext, 1, 2, Optional.empty(), register, homepageContent, () -> Optional.empty(), registerResolver, fieldsConfiguration, registerLinkService);

        assertThat(homePageView.getRegisterLinks().getRegistersLinkedFrom(), is(empty()));
        assertThat(homePageView.getRegisterLinks().getRegistersLinkedTo(), is(empty()));

        List<String> expectedLinkedFromRegisters = Arrays.asList("business");
        List<String> expectedLinkedToRegisters = Arrays.asList("company", "industry");

        when(registerLinkService.getRegisterLinks(registerName)).thenReturn(new RegisterLinks(expectedLinkedFromRegisters, expectedLinkedToRegisters));
        homePageView = new HomePageView(null, null, mockRequestContext, 1, 2, Optional.empty(), register, homepageContent, () -> Optional.empty(), registerResolver, fieldsConfiguration, registerLinkService);

        assertThat(homePageView.getRegisterLinks().getRegistersLinkedFrom(), equalTo(expectedLinkedFromRegisters));
        assertThat(homePageView.getRegisterLinks().getRegistersLinkedTo(), equalTo(expectedLinkedToRegisters));
    }

    @Test
    public void shouldGetSimilarRegistersIfAvailable() {
        RegisterMetadata registerMetadata = mock(RegisterMetadata.class);
        RegisterReadOnly register = mock(RegisterReadOnly.class);
        when(register.getRegisterMetadata()).thenReturn(registerMetadata);

        HomepageContent homepageContent = new HomepageContent(Optional.empty(), Optional.empty(), emptyList());
        HomePageView homePageView = new HomePageView(null, null, mockRequestContext, 1, 2, Optional.empty(), register, homepageContent, () -> Optional.empty(), registerResolver, fieldsConfiguration, registerLinkService);

        assertThat(homePageView.getHomepageContent().getSimilarRegisters(), is(empty()));

        List<String> similarRegisters = Arrays.asList("address", "territory");
        homepageContent = new HomepageContent(Optional.empty(), Optional.empty(), similarRegisters);
        homePageView = new HomePageView(null, null, mockRequestContext, 1, 2, Optional.empty(), register, homepageContent, () -> Optional.empty(), registerResolver, fieldsConfiguration, registerLinkService);

        assertThat(homePageView.getHomepageContent().getSimilarRegisters(), IsIterableContainingInOrder.contains("address", "territory"));
    }
}
