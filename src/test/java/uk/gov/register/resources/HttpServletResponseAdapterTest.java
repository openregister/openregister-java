package uk.gov.register.resources;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HttpServletResponseAdapterTest {
    @Mock
    private HttpServletResponse httpServletResponse;

    @Test
    public void addContentDispositionHeader_setsTheContentDispositionHeader() {
        HttpServletResponseAdapter httpServletResponseAdapter = new HttpServletResponseAdapter(httpServletResponse);

        httpServletResponseAdapter.addContentDispositionHeader("foo");

        verify(httpServletResponse).addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"foo\"");
    }

    @Test
    public void addLinkHeaderHeader_setsTheLinkHeader_ifNoLinkHeaderExistsAlready() {
        HttpServletResponseAdapter httpServletResponseAdapter = new HttpServletResponseAdapter(httpServletResponse);

        httpServletResponseAdapter.addLinkHeader("next", "bar");

        verify(httpServletResponse).setHeader("Link", "<bar>; rel=\"next\"");
    }

    @Test
    public void addLinkHeaderHeader_appendTheLinkHeader_ifLinkHeaderExistsAlready() {
        HttpServletResponseAdapter httpServletResponseAdapter = new HttpServletResponseAdapter(httpServletResponse);

        when(httpServletResponse.getHeader("Link")).thenReturn("<bar>; rel=\"next\"");

        httpServletResponseAdapter.addLinkHeader("previous", "foo");

        verify(httpServletResponse).setHeader("Link", "<bar>; rel=\"next\",<foo>; rel=\"previous\"");
    }

}