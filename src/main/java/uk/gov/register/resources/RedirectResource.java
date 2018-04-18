package uk.gov.register.resources;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriBuilder;

import java.net.URI;

import static javax.ws.rs.core.Response.status;

@Path("/")
public class RedirectResource {
    @GET
    @Path("/proof/entry/{entry-number}/{total-entries}/merkle:sha-256")
    public Response getProofRedirect(
            @Context HttpServletRequest request
    )
    {
        return redirectByPath(request, "/entry/", "/entries/");
    }

    @GET
    @Path("/item/sha-256:{item-hash}")
    public Response getEntryByNumberRedirect(
            @Context HttpServletRequest request
    )
    {
        return redirectByPath(request, "/item/", "/items/");
    }

    @GET
    @Path("/entry/{entry-number}")
    public Response getItemRedirect(
            @Context HttpServletRequest request
    )
    {
        return redirectByPath(request, "/entry/", "/entries/");
    }


    @GET
    @Path("/record/{record-key}")
    public Response getRecordByKeyRedirect(
            @Context HttpServletRequest request
    )
    {
        return redirectByPath(request, "/record/", "/records/");
    }
    @GET
    @Path("/record/{record-key}/entries")
    public Response getRecordEntriesRedirect(
            @Context HttpServletRequest request
    )
    {
        return redirectByPath(request, "/record/", "/records/");
    }



    private static ResponseBuilder movedPermanently(URI location) {
        return status(Response.Status.MOVED_PERMANENTLY).location(location);
    }

    public static Response redirectByPath(@Context HttpServletRequest request, String oldPath, String newPath) {
        String requestUrl = request.getRequestURL().toString();
        String redirectUrl = requestUrl.replaceFirst(oldPath, newPath);
        URI uri = UriBuilder.fromUri(redirectUrl).build();
        return movedPermanently(uri).build();
    }
}
