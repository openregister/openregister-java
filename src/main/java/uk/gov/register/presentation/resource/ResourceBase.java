package uk.gov.register.presentation.resource;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

public abstract class ResourceBase {
    public static final int ENTRY_LIMIT = 100;
    @Context
    protected HttpServletRequest httpServletRequest;

    protected String getRegisterPrimaryKey() {
        String host = httpServletRequest.getHeader("Host");

        if (host.contains(".")) {
            return host.substring(0, host.indexOf("."));
        } else {
            return host;
        }
    }
}

