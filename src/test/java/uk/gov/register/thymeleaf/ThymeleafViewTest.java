package uk.gov.register.thymeleaf;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.register.core.RegisterId;
import uk.gov.register.core.RegisterMetadata;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.resources.RequestContext;

import java.net.URI;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ThymeleafViewTest {
    @Mock
    private RequestContext requestContext;
    private ThymeleafView thymeleafView;

    @Before
    public void setUp() throws Exception {
        RegisterMetadata metadata = new RegisterMetadata(new RegisterId("company-limited-by-guarantee"), null, "Copyright text [with link](http://www.example.com/copyright)", null, null, null);
        RegisterReadOnly register = mock(RegisterReadOnly.class);
        when(register.getRegisterId()).thenReturn(new RegisterId("company-limited-by-guarantee"));
        when(register.getRegisterMetadata()).thenReturn(metadata);

        thymeleafView = new ThymeleafView(requestContext, "don't care", registerId -> URI.create("http://" + registerId + ".test.register.gov.uk"), register);
    }

    @Test
    public void friendlyRegisterName_convertsHyphensToUnderscores() throws Exception {

        assertThat(thymeleafView.getFriendlyRegisterName(), equalTo("Company limited by guarantee register"));
    }

    @Test
    @SuppressWarnings("OptionalGetWithoutIsPresent, should always be present for this test")
    public void getRenderedCopyrightText_returnsCopyrightRenderedAsMarkdown() throws Exception {
        String renderedCopyrightText = thymeleafView.getRenderedCopyrightText().get();

        assertThat(renderedCopyrightText, containsString("<p>Copyright text <a href=\"http://www.example.com/copyright\">with link</a></p>"));
    }

    @Test
    public void getIsGovukBranded_returnsTrueIfXForwardedHostIsOnRegisterGovuk() throws Exception {
        when(requestContext.getHost()).thenReturn("foo.register.gov.uk");

        assertThat(thymeleafView.getIsGovukBranded(), equalTo(true));
    }

    @Test
    public void getIsGovukBranded_returnsFalseIfXForwardedHostIsNotOnRegisterGovuk() throws Exception {
        when(requestContext.getHost()).thenReturn("foo.openregister.org");

        assertThat(thymeleafView.getIsGovukBranded(), equalTo(false));
    }
}
