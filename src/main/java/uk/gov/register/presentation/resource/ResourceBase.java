package uk.gov.register.presentation.resource;

import uk.gov.register.presentation.view.AbstractView;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

public abstract class ResourceBase {
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

    protected Response buildResponse(AbstractView abstractView) {
        String representation = httpServletRequest.getAttribute("representation").toString();

        if (representation.equals("html")) {
            return Response.ok().entity(abstractView.flatten()).header("Content-Type", MediaType.TEXT_HTML).build();
        } else {
            return Response.ok().entity(abstractView.getJsonNode()).header("Content-Type", MediaType.APPLICATION_JSON).build();
        }
    }

}
