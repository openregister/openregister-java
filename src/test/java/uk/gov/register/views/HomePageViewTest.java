package uk.gov.register.views;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.register.configuration.RegisterContentPages;
import uk.gov.register.core.RegisterMetadata;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.core.RegisterResolver;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HomePageViewTest {

    private final RegisterResolver registerResolver = registerName -> URI.create("http://" + registerName + ".test.register.gov.uk");
    @Mock
    RequestContext mockRequestContext;
    RegisterContentPages registerContentPages = new RegisterContentPages(Optional.empty());

    @Test
    public void getRegisterText_rendersRegisterTextAsMarkdown() throws Exception {
        String registerText = "foo *bar* [baz](/quux)";

        RegisterReadOnly register = mock(RegisterReadOnly.class);
        when(register.getRegisterName()).thenReturn("widget");
        when(register.getRegisterMetadata()).thenReturn(new RegisterMetadata("widget", null, null, null, registerText, "alpha"));

        HomePageView homePageView = new HomePageView(null, null, mockRequestContext, 1, 2, null, register, registerContentPages, () -> Optional.empty(), registerResolver);

        String result = homePageView.getRegisterText();

        assertThat(result, equalTo("<p>foo <em>bar</em> <a href=\"/quux\">baz</a></p>\n"));
    }

    @Test
    public void getLastUpdatedTime_formatsTheLocalDateTimeToUKDateTimeFormat() {
        Instant instant = LocalDateTime.of(2015, 9, 11, 13, 17, 59, 543).toInstant(ZoneOffset.UTC);

        HomePageView homePageView = new HomePageView(null, null, mockRequestContext, 1, 2, Optional.of(instant), null, registerContentPages, () -> Optional.empty(), registerResolver);

        assertThat(homePageView.getLastUpdatedTime(), equalTo("11 September 2015"));
    }

    @Test
    public void getLastUpdatedTime_returnsEmptyStringIfLastUpdatedTimeNotPresent() {
        HomePageView homePageView = new HomePageView(null, null, mockRequestContext, 1, 2, Optional.empty(), null, registerContentPages, () -> Optional.empty(), registerResolver);

        assertThat(homePageView.getLastUpdatedTime(), isEmptyString());
    }

    @Test
    public void getLinkToRegisterRegister_returnsTheLinkOfRegisterRegister(){
        Instant instant = LocalDateTime.of(2015, 9, 11, 13, 17, 59, 543).toInstant(ZoneOffset.UTC);

        when(mockRequestContext.getScheme()).thenReturn("https");

        RegisterReadOnly register = mock(RegisterReadOnly.class);
        when(register.getRegisterName()).thenReturn("school");
        HomePageView homePageView = new HomePageView(null, null, mockRequestContext, 1, 2, Optional.of(instant), register, registerContentPages, () -> Optional.empty(), registerResolver);

        assertThat(homePageView.getLinkToRegisterRegister(), equalTo(URI.create("http://register.test.register.gov.uk/record/school")));
    }

    @Test
    public void shouldDisplayHistoryPageIfAvailable() {
        RegisterContentPages registerContentPages = new RegisterContentPages(Optional.empty());
        HomePageView homePageView = new HomePageView(null, null, mockRequestContext, 1, 2, Optional.empty(), null, registerContentPages, () -> Optional.empty(), registerResolver);

        assertThat(homePageView.getContentPages().getRegisterHistoryPageUrl().isPresent(), is(false));

        String historyUrl = "http://register-history.openregister.org";
        registerContentPages = new RegisterContentPages(Optional.of(historyUrl));
        homePageView = new HomePageView(null, null, mockRequestContext, 1, 2, Optional.empty(), null, registerContentPages, () -> Optional.empty(), registerResolver);

        assertThat(homePageView.getContentPages().getRegisterHistoryPageUrl().isPresent(), is(true));
        assertThat(homePageView.getContentPages().getRegisterHistoryPageUrl().get(), is(historyUrl));
    }
}
