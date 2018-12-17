package uk.gov.register.resources.v2;

import com.codahale.metrics.annotation.Timed;
import uk.gov.register.core.EntryLog;
import uk.gov.register.core.EntryType;
import uk.gov.register.core.RegisterContext;
import uk.gov.register.proofs.ProofGenerator;
import uk.gov.register.util.HashValue;
import uk.gov.register.views.RegisterProof;
import uk.gov.register.views.v2.AuditPathView;
import uk.gov.register.views.v2.RootHashView;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/next/proofs")
public class VerifiableLogResource {
    private final EntryLog entryLog;
    private final RegisterContext registerContext;

    @Inject
    public VerifiableLogResource(RegisterContext registerContext) {
        this.registerContext = registerContext;
        this.entryLog = registerContext.buildEntryLog();
    }

    @GET
    @Path("/register")
    @Produces({MediaType.APPLICATION_JSON})
    @Timed
    public RootHashView registerProof() {
        return registerContext.withVerifiableLog(verifiableLog -> {
            int totalEntries = entryLog.getTotalEntries(EntryType.user);
            RegisterProof proof = new ProofGenerator(verifiableLog).getRegisterProof(totalEntries);

            return new RootHashView(proof.getRootHash(), proof.getTotalEntries());
        });
    }

    @GET
    @Path("/entry/{entry-number}/{total-entries}")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public AuditPathView entryProof(@PathParam("entry-number") int entryNumber, @PathParam("total-entries") int totalEntries) {
        validateEntryProofParams(entryNumber, totalEntries);
        List<HashValue> auditPath = registerContext.withVerifiableLog(verifiableLog -> new ProofGenerator(verifiableLog).getLeafAuditPath(entryNumber, totalEntries));
        return new AuditPathView(auditPath);
    }

    @GET
    @Path("/consistency/{total-entries-1}/{total-entries-2}")
    @Produces({MediaType.APPLICATION_JSON})
    @Timed
    public AuditPathView consistencyProof(@PathParam("total-entries-1") int totalEntries1, @PathParam("total-entries-2") int totalEntries2) {
        validateConsistencyProofParams(totalEntries1, totalEntries2);
        List<HashValue> auditPath = registerContext.withVerifiableLog(verifiableLog -> new ProofGenerator(verifiableLog).getAuditPath(totalEntries1, totalEntries2));
        return new AuditPathView(auditPath);
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
