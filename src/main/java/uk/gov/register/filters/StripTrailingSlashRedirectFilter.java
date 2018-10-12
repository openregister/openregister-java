package uk.gov.register.filters;

import com.google.common.net.HttpHeaders;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class StripTrailingSlashRedirectFilter implements javax.servlet.Filter {

    private static String removeLastChar(String str) {
        return str.substring(0, str.length() - 1);
    }

    @Override
    public void init(FilterConfig filterConfig) { }

    @Override
    public void destroy() { }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest)request;

        String path = httpRequest.getRequestURI();
        if (path.endsWith("/") && path.length() > 1) {
        int serverPort = httpRequest.getServerPort();
            StringBuilder location = new StringBuilder();
            location.append(request.getScheme())
                    .append("://")
                    .append(httpRequest.getServerName());
            if (!(serverPort == 80 || serverPort == 443)) {
                location.append(":").append(httpRequest.getServerPort());
            }
            location.append(removeLastChar(httpRequest.getRequestURI()));

            String queryString = httpRequest.getQueryString();
            if (queryString != null) {
                location.append('?');
                location.append(queryString);
            }

            HttpServletResponse httpResponse = (HttpServletResponse)response;
            httpResponse.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
            httpResponse.setHeader(HttpHeaders.LOCATION, location.toString());
            return;
        }

        chain.doFilter(request, response);
    }
}
