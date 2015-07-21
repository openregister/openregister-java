package uk.gov.register.presentation.resource;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import java.net.URI;

public abstract class ResourceBase {
    public static final int ENTRY_LIMIT = 100;
    @Context
    protected HttpServletRequest httpServletRequest;

    //Note: copied the logic to fetch primary key from alpha register.
    //Note: We might need to change the logic of extracting register primary key for beta registers
    protected String getRegisterPrimaryKey() {
        try {
            String host = new URI(httpServletRequest.getRequestURL().toString()).getHost();

            //hack for functional tests
            if (host.startsWith("localhost")) return "ft_test_pkey";
            else return host.replaceAll("([^\\.]+)\\.(openregister)\\..*", "$1");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

