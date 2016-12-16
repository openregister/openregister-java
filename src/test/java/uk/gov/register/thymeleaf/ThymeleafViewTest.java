package uk.gov.register.thymeleaf;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.register.core.RegisterMetadata;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.resources.RequestContext;

import java.net.URI;
import java.util.Optional;

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
        RegisterMetadata metadata = new RegisterMetadata("company-limited-by-guarantee", null, "Copyright text [with link](http://www.example.com/copyright)", null, null, null);
        RegisterReadOnly register = mock(RegisterReadOnly.class);
        when(register.getRegisterName()).thenReturn("company-limited-by-guarantee");
        when(register.getRegisterMetadata()).thenReturn(metadata);

        thymeleafView = new ThymeleafView(requestContext, "don't care", () -> Optional.empty(), registerName -> URI.create("http://" + registerName + ".test.register.gov.uk"), register);
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
}
