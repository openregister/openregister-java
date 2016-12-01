package uk.gov.register.views;

import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.register.configuration.RegisterContentPages;
import uk.gov.register.core.RegisterData;
import uk.gov.register.resources.RequestContext;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.text.IsEmptyString.isEmptyString;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HomePageViewTest {

    @Mock
    RequestContext mockRequestContext;
    RegisterContentPages registerContentPages = new RegisterContentPages(Optional.empty());

    @Test
    public void getRegisterText_rendersRegisterTextAsMarkdown() throws Exception {
        String registerText = "foo *bar* [baz](/quux)";
        RegisterData registerData = new RegisterData(ImmutableMap.of(
                "register", new TextNode("widget"),
                "phase", new TextNode("alpha"),
                "text", new TextNode(registerText)
        ));
        HomePageView homePageView = new HomePageView(null, null, mockRequestContext, 1, 2, null, () -> "test.register.gov.uk", registerData, registerContentPages, () -> Optional.empty());

        String result = homePageView.getRegisterText();

        assertThat(result, equalTo("<p>foo <em>bar</em> <a href=\"/quux\">baz</a></p>\n"));
    }

    @Test
    public void getLastUpdatedTime_formatsTheLocalDateTimeToUKDateTimeFormat() {
        Instant instant = LocalDateTime.of(2015, 9, 11, 13, 17, 59, 543).toInstant(ZoneOffset.UTC);

        HomePageView homePageView = new HomePageView(null, null, mockRequestContext, 1, 2, Optional.of(instant), () -> "test.register.gov.uk", null, registerContentPages, () -> Optional.empty());

        assertThat(homePageView.getLastUpdatedTime(), equalTo("11 September 2015"));
    }

    @Test
    public void getLastUpdatedTime_returnsEmptyStringIfLastUpdatedTimeNotPresent() {
        HomePageView homePageView = new HomePageView(null, null, mockRequestContext, 1, 2, Optional.empty(), () -> "test.register.gov.uk", null, registerContentPages, () -> Optional.empty());

        assertThat(homePageView.getLastUpdatedTime(), isEmptyString());
    }

    @Test
    public void getLinkToRegisterRegister_returnsTheLinkOfRegisterRegister(){
        Instant instant = LocalDateTime.of(2015, 9, 11, 13, 17, 59, 543).toInstant(ZoneOffset.UTC);

        when(mockRequestContext.getScheme()).thenReturn("https");

        RegisterData registerData = new RegisterData(ImmutableMap.of(
                "register", new TextNode("school")
        ));
        HomePageView homePageView = new HomePageView(null, null, mockRequestContext, 1, 2, Optional.of(instant), () -> "test.register.gov.uk", registerData, registerContentPages, () -> Optional.empty());

        assertThat(homePageView.getLinkToRegisterRegister(), equalTo(URI.create("https://register.test.register.gov.uk/record/school")));
    }

    @Test
    public void shouldDisplayHistoryPageIfAvailable() {
        RegisterContentPages registerContentPages = new RegisterContentPages(Optional.empty());
        HomePageView homePageView = new HomePageView(null, null, mockRequestContext, 1, 2, Optional.empty(), () -> "test.register.gov.uk", null, registerContentPages, () -> Optional.empty());

        assertThat(homePageView.getContentPages().getRegisterHistoryPageUrl().isPresent(), is(false));

        String historyUrl = "http://register-history.openregister.org";
        registerContentPages = new RegisterContentPages(Optional.of(historyUrl));
        homePageView = new HomePageView(null, null, mockRequestContext, 1, 2, Optional.empty(), () -> "test.register.gov.uk", null, registerContentPages, () -> Optional.empty());

        assertThat(homePageView.getContentPages().getRegisterHistoryPageUrl().isPresent(), is(true));
        assertThat(homePageView.getContentPages().getRegisterHistoryPageUrl().get(), is(historyUrl));
    }
}
