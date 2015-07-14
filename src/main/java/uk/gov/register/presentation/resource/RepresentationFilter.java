package uk.gov.register.presentation.resource;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RepresentationFilter implements Filter {
    Pattern pattern = Pattern.compile("(.+?)\\.?([a-zA-Z]+)?");

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequestWrapper wrappedRequest = new HttpServletRequestWrapper((HttpServletRequest)request){
            @Override
            public StringBuffer getRequestURL() {
                return new StringBuffer(newRequestURI(super.getRequestURL().toString()));
            }

            @Override
            public String getRequestURI() {
                return newRequestURI(super.getRequestURI());
            }

            private String newRequestURI(String requestURL) {
                Matcher matcher = pattern.matcher(requestURL);
                if(matcher.matches()){
                    String requestUrlWithoutRepresentation = matcher.group(1);
                    String representation = matcher.group(2);
                    if(StringUtils.isEmpty(representation)){
                        representation = "html";
                    }
                    request.setAttribute("representation", representation);
                    return requestUrlWithoutRepresentation;
                }
                return requestURL;
            }
        };
        chain.doFilter(wrappedRequest, response);
    }

    @Override
    public void destroy() {

    }
}
