package uk.gov.register.resources.v1;

import com.codahale.metrics.annotation.Timed;
import uk.gov.register.core.EntryLog;
import uk.gov.register.core.EntryType;
import uk.gov.register.core.RegisterContext;
import uk.gov.register.proofs.ProofGenerator;
import uk.gov.register.views.ConsistencyProof;
import uk.gov.register.views.EntryProof;
import uk.gov.register.views.RegisterProof;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static uk.gov.register.resources.RedirectResource.redirectByPath;

@Path("/proof")
public class VerifiableLogResource {
    private final RegisterContext registerContext;
    private final EntryLog entryLog;

    @Inject
    public VerifiableLogResource(RegisterContext registerContext) {
        this.registerContext = registerContext;
        this.entryLog = registerContext.buildEntryLog();
    }

    @GET
    @Path("/register/merkle:sha-256")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public RegisterProof registerProof() {
        return registerContext.withV1VerifiableLog(verifiableLog -> {
            int totalEntries = entryLog.getTotalEntries(EntryType.user);
            return new ProofGenerator(verifiableLog).getRegisterProof(totalEntries);
        });
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
        return registerContext.withV1VerifiableLog(verifiableLog -> new ProofGenerator(verifiableLog).getEntryProof(entryNumber, totalEntries));
    }

    @GET
    @Path("/consistency/{total-entries-1}/{total-entries-2}/merkle:sha-256")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public ConsistencyProof consistencyProof(@PathParam("total-entries-1") int totalEntries1, @PathParam("total-entries-2") int totalEntries2) {
        validateConsistencyProofParams(totalEntries1, totalEntries2);
        return registerContext.withV1VerifiableLog(verifiableLog -> new ProofGenerator(verifiableLog).getConsistencyProof(totalEntries1, totalEntries2));
    }

    private void validateEntryProofParams(int entryNumber, int totalEntries) {
        if (entryNumber < 1 ||
                entryNumber > totalEntries ||
                totalEntries > entryLog.getTotalEntries(EntryType.user)) {
            throw new BadRequestException("Invalid parameters");
        }
    }

    private void validateConsistencyProofParams(int totalEntries1, int totalEntries2) {
        if (totalEntries1 < 1 ||
                totalEntries1 > totalEntries2 ||
                totalEntries2 > entryLog.getTotalEntries(EntryType.user)) {
            throw new BadRequestException("Invalid parameters");
        }
    }
}
