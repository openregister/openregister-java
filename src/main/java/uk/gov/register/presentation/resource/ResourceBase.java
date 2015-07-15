package uk.gov.register.presentation.resource;

import uk.gov.register.presentation.entity.Entity;
import uk.gov.register.presentation.entity.Representation;
import uk.gov.register.presentation.view.AbstractView;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
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

    protected Response buildResponse(AbstractView abstractView) {
        String representation = httpServletRequest.getAttribute("representation").toString();

        Entity entity = Representation.valueOf(representation).entity;
        return Response.ok().entity(entity.convert(abstractView)).header("Content-Type", entity.contentType()).build();
    }
}

