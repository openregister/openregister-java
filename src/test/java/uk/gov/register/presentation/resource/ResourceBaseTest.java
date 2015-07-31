package uk.gov.register.presentation.resource;

import org.junit.Test;

import javax.servlet.http.HttpServletRequest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResourceBaseTest {
    @Test
    public void takesRegisterNameFromHttpHost() throws Exception {
        ResourceBase resourceBase = new TestResourceBase("school.beta.openregister.org");

        String registerPrimaryKey = resourceBase.getRegisterPrimaryKey();

        assertThat(registerPrimaryKey, equalTo("school"));
    }

    @Test
    public void behavesGracefullyWhenGivenHostWithNoDots() throws Exception {
        ResourceBase resourceBase = new TestResourceBase("school");

        String registerPrimaryKey = resourceBase.getRegisterPrimaryKey();

        assertThat(registerPrimaryKey, equalTo("school"));
    }

    public class TestResourceBase extends ResourceBase {
        public TestResourceBase(String hostHeader) {
            this.httpServletRequest = mock(HttpServletRequest.class);
            when(httpServletRequest.getHeader("Host")).thenReturn(hostHeader);
        }
    }
}
