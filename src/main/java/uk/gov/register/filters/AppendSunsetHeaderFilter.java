package uk.gov.register.filters;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AppendSunsetHeaderFilter implements javax.servlet.Filter
{
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse httpServletResponse = ((HttpServletResponse) response);
        httpServletResponse.addHeader("Sunset", "15 March 2021 23:59:59 GMT");
        httpServletResponse.addHeader("Link", "<https://www.registers.service.gov.uk/>; rel=\"deprecation\"; type=\"text/html\"");

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
}
