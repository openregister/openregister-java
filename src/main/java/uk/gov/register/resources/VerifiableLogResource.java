package uk.gov.register.resources;

import uk.gov.register.views.EntryProof;
import uk.gov.register.views.ConsistencyProof;
import uk.gov.register.views.RegisterProof;
import uk.gov.register.db.EntryMerkleLeafStore;
import uk.gov.register.db.EntryQueryDAO;
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
    @Path("/entry/{entry-number}/{total-entries}/merkle:sha-256")
    @Produces(MediaType.APPLICATION_JSON)
    public EntryProof entryProof(@PathParam("entry-number") int entryNumber, @PathParam("total-entries") int totalEntries) throws NoSuchAlgorithmException {
        try {
            entryDAO.begin();
            VerifiableLog verifiableLog = createVerifiableLog(entryDAO);

            List<String> auditProof = verifiableLog.auditProof(entryNumber, totalEntries).stream()
                    .map(this::bytesToString).collect(Collectors.toList());

            return new EntryProof(Integer.toString(entryNumber), auditProof);
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
