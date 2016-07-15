package uk.gov.register.presentation.resource;

import uk.gov.register.presentation.AuditProof;
import uk.gov.register.presentation.ConsistencyProof;
import uk.gov.register.presentation.RegisterProof;
import uk.gov.register.presentation.dao.EntryMerkleLeafStore;
import uk.gov.register.presentation.dao.EntryQueryDAO;
import uk.gov.verifiablelog.VerifiableLog;
import uk.gov.verifiablelog.store.memoization.MemoizationStore;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Collectors;

@Path("/proof")
public class VerifiableLogResource {

    private final EntryQueryDAO entryDAO;
    private final MemoizationStore memoizationStore;

    @Inject
    public VerifiableLogResource(EntryQueryDAO entryDAO, MemoizationStore memoizationStore) {
        this.entryDAO = entryDAO;
        this.memoizationStore = memoizationStore;
    }

    @GET
    @Path("/register/merkle:sha-256")
    @Produces(MediaType.APPLICATION_JSON)
    public RegisterProof registerProof() throws NoSuchAlgorithmException {
        try {
            entryDAO.begin();
            VerifiableLog verifiableLog = createVerifiableLog(entryDAO);
            String rootHash = bytesToString(verifiableLog.currentRoot());
            return new RegisterProof(rootHash);
        } finally {
            entryDAO.rollback();
        }
    }

    @GET
    @Path("/entry/{total-entries}/{entry-number}/merkle:sha-256")
    @Produces(MediaType.APPLICATION_JSON)
    public AuditProof auditProof(@PathParam("total-entries") int totalEntries, @PathParam("entry-number") int entryNumber) throws NoSuchAlgorithmException {
        try {
            entryDAO.begin();
            VerifiableLog verifiableLog = createVerifiableLog(entryDAO);

            List<String> auditProof = verifiableLog.auditProof(entryNumber, totalEntries).stream()
                    .map(this::bytesToString).collect(Collectors.toList());

            return new AuditProof(Integer.toString(entryNumber), auditProof);
        } finally {
            entryDAO.rollback();
        }
    }

    @GET
    @Path("/consistency/{total-entries-1}/{total-entries-2}/merkle:sha-256")
    @Produces(MediaType.APPLICATION_JSON)
    public ConsistencyProof consistencyProof(@PathParam("total-entries-1") int totalEntries1, @PathParam("total-entries-2") int totalEntries2) throws NoSuchAlgorithmException {
        try {
            entryDAO.begin();
            VerifiableLog verifiableLog = createVerifiableLog(entryDAO);

            List<String> consistencyProof = verifiableLog.consistencyProof(totalEntries1, totalEntries2).stream()
                    .map(this::bytesToString).collect(Collectors.toList());

            return new ConsistencyProof(consistencyProof);
        } finally {
            entryDAO.rollback();
        }
    }

    private VerifiableLog createVerifiableLog(EntryQueryDAO entryDAO) throws NoSuchAlgorithmException {
        return new VerifiableLog(MessageDigest.getInstance("SHA-256"), new EntryMerkleLeafStore(entryDAO), memoizationStore);
    }

    private String bytesToString(byte[] bytes) {
        return DatatypeConverter.printHexBinary(bytes).toLowerCase();
    }
}
