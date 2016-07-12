package uk.gov.register.thymeleaf;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.register.presentation.config.Register;
import uk.gov.register.presentation.resource.RequestContext;

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
        thymeleafView = new ThymeleafView(requestContext, "don't care");
    }

    @Test
    public void friendlyRegisterName_convertsHyphensToUnderscores() throws Exception {
        when(requestContext.getRegisterPrimaryKey()).thenReturn("company-limited-by-guarantee");

        assertThat(thymeleafView.getFriendlyRegisterName(), equalTo("Company limited by guarantee register"));
    }

    @Test
    @SuppressWarnings("OptionalGetWithoutIsPresent, should always be present for this test")
    public void getRenderedCopyrightText_returnsCopyrightRenderedAsMarkdown() throws Exception {
        Register theRegister = mock(Register.class);
        when(theRegister.getCopyright()).thenReturn(Optional.of("Copyright text [with link](http://www.example.com/copyright)"));
        when(requestContext.getRegister()).thenReturn(theRegister);

        String renderedCopyrightText = thymeleafView.getRenderedCopyrightText().get();

        assertThat(renderedCopyrightText, containsString("<p>Copyright text <a href=\"http://www.example.com/copyright\">with link</a></p>"));
    }
}
