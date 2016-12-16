package uk.gov.register.resources;

import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

@Service
public class RequestContext implements SchemeContext {

    @Context
    HttpServletRequest httpServletRequest;

    @Context
    ServletContext servletContext;

    @Context
    HttpServletResponse httpServletResponse;

    @Inject
    public RequestContext() {
    }

    @Override
    public String getScheme() {
        Optional<String> header = Optional.ofNullable(httpServletRequest.getHeader("X-Forwarded-Proto"));
        return header.orElse(httpServletRequest.getScheme());
    }

    public String getHost() {
        return firstNonNull(httpServletRequest.getHeader("X-Forwarded-Host"),
                httpServletRequest.getHeader("Host"));
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

    public Optional<String> resourceExtension() {
        String requestURI = httpServletRequest.getRequestURI();
        if (requestURI.lastIndexOf('.') == -1) {
            return Optional.empty();
        }
        String[] tokens = requestURI.split("\\.");
        return Optional.of(tokens[tokens.length - 1]);
    }
}
