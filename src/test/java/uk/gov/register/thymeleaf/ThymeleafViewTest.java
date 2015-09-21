package uk.gov.register.thymeleaf;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.register.presentation.resource.RequestContext;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
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
}
