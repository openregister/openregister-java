package uk.gov.register.presentation.resource;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

public abstract class ResourceBase {
    public static final int ENTRY_LIMIT = 100;
    @Context
    protected HttpServletRequest httpServletRequest;

    //Note: copied the logic to fetch primary key from alpha register.
    //Note: We might need to change the logic of extracting register primary key for beta registers
    protected String getRegisterPrimaryKey() {
        String host = httpServletRequest.getHeader("Host");

        if (host.contains(".")) {
            return host.substring(0, host.indexOf("."));
        } else {
            return host;
        }
    }
}

