package uk.gov.register.thymeleaf;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.collections.Sets;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.register.presentation.config.Register;
import uk.gov.register.presentation.resource.RequestContext;

import java.time.LocalDateTime;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HomePageViewTest {

    @Mock
    RequestContext mockRequestContext;

    @Test
    public void getRegisterText_rendersRegisterTextAsMarkdown() throws Exception {
        HomePageView homePageView = new HomePageView(null, null, mockRequestContext, 1, null);

        String registerText = "foo *bar* [baz](/quux)";
        when(mockRequestContext.getRegister()).thenReturn(new Register("", Sets.newSet(), "", "", registerText));

        String result = homePageView.getRegisterText();

        assertThat(result, equalTo("<p>foo <em>bar</em> <a href=\"/quux\">baz</a></p>\n"));
    }

    @Test
    public void getLastUpdatedTime_formatsTheLocalDateTimeToUKDateTimeFormat() {
        LocalDateTime localDateTime = LocalDateTime.of(2015, 9, 11, 13, 17, 59, 543);

        HomePageView homePageView = new HomePageView(null, null, mockRequestContext, 1, localDateTime);

        assertThat(homePageView.getLastUpdatedTime(), equalTo("11 Sep 2015"));
    }
}
