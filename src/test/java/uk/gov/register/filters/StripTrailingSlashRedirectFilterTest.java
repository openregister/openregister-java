package uk.gov.register.filters;

import org.junit.Test;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.Mockito.*;

public class StripTrailingSlashRedirectFilterTest {

     private HttpServletResponse makeRequestToPath(String path) throws IOException, ServletException {
      return makeRequestToPath(path, null, 443);
    }

    private HttpServletResponse makeRequestToPath(String path, String queryString) throws IOException, ServletException {
      return makeRequestToPath(path, queryString, 443);
    }

    private HttpServletResponse makeRequestToPath(String path, String queryString, int port) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        when(httpServletRequest.getRequestURI()).thenReturn(path);
        when(httpServletRequest.getServerName()).thenReturn("country.register.gov.uk");
        when(httpServletRequest.getScheme()).thenReturn("https");
        when(httpServletRequest.getQueryString()).thenReturn(queryString);
        when(httpServletRequest.getServerPort()).thenReturn(port);
        StripTrailingSlashRedirectFilter stripTrailingSlashRedirectFilter = new StripTrailingSlashRedirectFilter();
        stripTrailingSlashRedirectFilter.doFilter(httpServletRequest, httpServletResponse,
                filterChain);
        return httpServletResponse;
    }

    @Test
    public void redirectsTrailingSlash() throws IOException, ServletException {
        HttpServletResponse response = makeRequestToPath("/records/");
        verify(response).setStatus(301);
        verify(response).setHeader("Location", "https://country.register.gov.uk/records");
    }

    @Test
    public void doesNotRedirectRootPath() throws IOException, ServletException {
        HttpServletResponse response = makeRequestToPath("/");
        verify(response, never()).setStatus(301);
    }

    @Test
    public void doesNotRedirectPathWithoutTrailingSlash() throws IOException, ServletException {
        HttpServletResponse response = makeRequestToPath("/records");
        verify(response, never()).setStatus(301);
    }

    @Test
    public void retainsQueryString() throws IOException, ServletException {
        HttpServletResponse response = makeRequestToPath("/records/", "page-size=5000");
        verify(response).setStatus(301);
        verify(response).setHeader("Location", "https://country.register.gov.uk/records?page-size=5000");
    }

    @Test
    public void retainsCustomPort() throws IOException, ServletException {
        HttpServletResponse response = makeRequestToPath("/records/", "page-size=5000", 8080);
        verify(response).setStatus(301);
        verify(response).setHeader("Location", "https://country.register.gov.uk:8080/records?page-size=5000");
    }
}