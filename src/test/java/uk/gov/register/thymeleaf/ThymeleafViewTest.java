package uk.gov.register.thymeleaf;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.register.core.EmptyRegister;
import uk.gov.register.core.RegisterMetadata;
import uk.gov.register.resources.RequestContext;

import java.net.URI;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ThymeleafViewTest {
    @Mock
    private RequestContext requestContext;
    private ThymeleafView thymeleafView;

    @Before
    public void setUp() throws Exception {
        RegisterMetadata registerMetadata = new RegisterMetadata("company-limited-by-guarantee", Collections.emptyList(), "Copyright text [with link](http://www.example.com/copyright)", null, null, null);
        EmptyRegister register = new EmptyRegister(registerMetadata);
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
