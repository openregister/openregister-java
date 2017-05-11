package uk.gov.register.filters;

import com.google.common.net.HttpHeaders;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
public class HttpToHttpsRedirectFilter implements javax.servlet.Filter {

   @Override
   public void init(FilterConfig filterConfig) throws ServletException { }

   @Override
   public void destroy() { }

   @Override
   public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
      HttpServletRequest httpRequest = (HttpServletRequest)request;

      if ("http".equals(httpRequest.getHeader(HttpHeaders.X_FORWARDED_PROTO))) {
         StringBuilder location = new StringBuilder();
         location.append("https://");
         location.append(httpRequest.getServerName());
         location.append(httpRequest.getRequestURI());

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
