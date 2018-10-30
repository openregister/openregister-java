package uk.gov.register.resources;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriBuilder;

import java.net.URI;

import static javax.ws.rs.core.Response.status;

@Path("/")
public class RedirectResource {

    /*
     * From v2 onwards every URL will have a version in it, but v1 actually used unversioned
     * URLs. While v1 is still available, we should return a helpful response if people try to
     * add /v1/ to the URL.
     */
    @GET
    @Path("/v1/{resource:.+}")
    public Response redirectV1GetRequests(@Context HttpServletRequest request, @PathParam("resource") String resource) {
        return redirectByPath(request, "/v1/" + resource, "/" + resource, Response.Status.TEMPORARY_REDIRECT);
    }

    @POST
    @Path("/v1/{resource:.+}")
    public Response redirectV1PostRequests(@Context HttpServletRequest request, @PathParam("resource") String resource) {
        return redirectByPath(request, "/v1/" + resource, "/" + resource, Response.Status.TEMPORARY_REDIRECT);
    }

    @DELETE
    @Path("/v1/{resource:.+}")
    public Response redirectV1DeleteRequests(@Context HttpServletRequest request, @PathParam("resource") String resource) {
        return redirectByPath(request, "/v1/" + resource, "/" + resource, Response.Status.TEMPORARY_REDIRECT);
    }

    @PUT
    @Path("/v1/{resource:.+}")
    public Response redirectV1PutRequests(@Context HttpServletRequest request, @PathParam("resource") String resource) {
        return redirectByPath(request, "/v1/" + resource, "/" + resource, Response.Status.TEMPORARY_REDIRECT);
    }

    @HEAD
    @Path("/v1/{resource:.+}")
    public Response redirectV1HeadRequests(@Context HttpServletRequest request, @PathParam("resource") String resource) {
        return redirectByPath(request, "/v1/" + resource, "/" + resource, Response.Status.TEMPORARY_REDIRECT);
    }

    @OPTIONS
    @Path("/v1/{resource:.+}")
    public Response redirectV1OptionsRequests(@Context HttpServletRequest request, @PathParam("resource") String resource) {
        return redirectByPath(request, "/v1/" + resource, "/" + resource, Response.Status.TEMPORARY_REDIRECT);
    }

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

    public static Response redirectByPath(@Context HttpServletRequest request, String oldPath, String newPath, Response.Status responseStatus) {
        String requestUrl = request.getRequestURL().toString();
        String redirectUrl = requestUrl.replaceFirst(oldPath, newPath);
        URI uri = UriBuilder.fromUri(redirectUrl).build();
        return status(responseStatus).location(uri).build();
    }

    public static Response redirectByPath(@Context HttpServletRequest request, String oldPath, String newPath) {
        return redirectByPath(request, oldPath, newPath, Response.Status.MOVED_PERMANENTLY);
    }
}
