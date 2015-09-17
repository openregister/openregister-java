package uk.gov.register.thymeleaf;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.register.presentation.resource.RequestContext;

import javax.servlet.http.HttpServletRequest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ThymeleafViewTest {
    @Mock
    private HttpServletRequest httpServletRequest;
    private ThymeleafView thymeleafView;

    @Before
    public void setUp() throws Exception {
        thymeleafView = new ThymeleafView(new RequestContext() {
            @Override
            public HttpServletRequest getHttpServletRequest() {
                return httpServletRequest;
            }
        }, "don't care");
    }

    @Test
    public void friendlyRegisterName_convertsHyphensToUnderscores() throws Exception {
        when(httpServletRequest.getHeader("Host")).thenReturn("company-limited-by-guarantee.openregister.org");

        assertThat(thymeleafView.getFriendlyRegisterName(), equalTo("Company limited by guarantee register"));
    }
}
