package uk.gov.register.thymeleaf;

import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.register.core.RegisterData;
import uk.gov.register.resources.RequestContext;

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
        RegisterData register = new RegisterData(ImmutableMap.of(
                "register", new TextNode("company-limited-by-guarantee"),
                "copyright", new TextNode("Copyright text [with link](http://www.example.com/copyright)")));
        thymeleafView = new ThymeleafView(requestContext, "don't care", register, () -> "test.register.gov.uk", () -> Optional.empty());
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
