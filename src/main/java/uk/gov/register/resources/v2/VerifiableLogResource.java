package uk.gov.register.resources.v2;

import com.codahale.metrics.annotation.Timed;
import uk.gov.register.core.EntryType;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.resources.FutureAPI;
import uk.gov.register.views.ConsistencyProof;
import uk.gov.register.views.EntryProof;
import uk.gov.register.views.RegisterProof;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static uk.gov.register.resources.RedirectResource.redirectByPath;

@FutureAPI
@Path("/v2/proof")
public class VerifiableLogResource {
    private final RegisterReadOnly register;

    @Inject
    public VerifiableLogResource(RegisterReadOnly register) {
        this.register = register;
    }

    @GET
    @Path("/register/merkle:sha-256")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public RegisterProof registerProof() {
        return register.getRegisterProof();
    }

    @GET
    @Path("/entry/{entry-number}/{total-entries}/merkle:sha-256")
    public Response getProofRedirect(
            @Context HttpServletRequest request
    )
    {
        return redirectByPath(request, "/entry/", "/entries/");
    }

    @GET
    @Path("/entries/{entry-number}/{total-entries}/merkle:sha-256")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public EntryProof entryProof(@PathParam("entry-number") int entryNumber, @PathParam("total-entries") int totalEntries) {
        validateEntryProofParams(entryNumber, totalEntries);
        return register.getEntryProof(entryNumber, totalEntries);
    }

    @GET
    @Path("/consistency/{total-entries-1}/{total-entries-2}/merkle:sha-256")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public ConsistencyProof consistencyProof(@PathParam("total-entries-1") int totalEntries1, @PathParam("total-entries-2") int totalEntries2) {
        validateConsistencyProofParams(totalEntries1, totalEntries2);
        return register.getConsistencyProof(totalEntries1, totalEntries2);
    }

    private void validateEntryProofParams(int entryNumber, int totalEntries) {
        if (entryNumber < 1 ||
                entryNumber > totalEntries ||
                totalEntries > register.getTotalEntries(EntryType.user)) {
            throw new BadRequestException("Invalid parameters");
        }
    }

    private void validateConsistencyProofParams(int totalEntries1, int totalEntries2) {
        if (totalEntries1 < 1 ||
                totalEntries1 > totalEntries2 ||
                totalEntries2 > register.getTotalEntries(EntryType.user)) {
            throw new BadRequestException("Invalid parameters");
        }
    }
}
