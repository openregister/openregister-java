package uk.gov.register.presentation.resource;

import org.jvnet.hk2.annotations.Service;
import uk.gov.register.presentation.RegisterNameExtractor;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;

@Service
public class RequestContext {

    @Context
    HttpServletRequest httpServletRequest;

    @Context
    ServletContext servletContext;

    @Context
    HttpServletResponse httpServletResponse;

    public String requestURI() {
        return getHttpServletRequest().getRequestURI();
    }

    public String requestUrl() {
        return getHttpServletRequest().getRequestURL().toString();
    }

    public String getRegisterPrimaryKey() {
        return RegisterNameExtractor.extractRegisterName(httpServletRequest.getHeader("Host"));
    }

    public HttpServletRequest getHttpServletRequest() {
        return httpServletRequest;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    public HttpServletResponse getHttpServletResponse() {
        return httpServletResponse;
    }
}
