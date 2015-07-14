package uk.gov.register.presentation.resource;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

public abstract class ResourceBase {
    @Context
    protected UriInfo uriInfo;

    //Note: copied the logic to fetch primary key from alpha register.
    //Note: We might need to change the logic of extracting register primary key for beta registers
    String getRegisterPrimaryKey() {
        try {
            String host = uriInfo.getAbsolutePath().getHost();

            //hack for functional tests
            if (host.startsWith("localhost")) return "ft_test_pkey";
            else return host.replaceAll("([^\\.]+)\\.(openregister)\\..*", "$1");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
