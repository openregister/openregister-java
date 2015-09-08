package uk.gov.register.presentation.representations;

import org.jvnet.hk2.annotations.Service;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

@Service
public class RequestContext {

    @Context
    private HttpServletRequest httpServletRequest;

    public String requestUrl() {
        return httpServletRequest.getRequestURL().toString();
    }
}
