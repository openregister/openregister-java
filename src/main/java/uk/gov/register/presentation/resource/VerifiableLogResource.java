package uk.gov.register.presentation.resource;

import uk.gov.register.presentation.RegisterProof;
import uk.gov.register.presentation.dao.EntryQueryDAO;
import uk.gov.register.presentation.dao.EntryMerkleLeafStore;
import uk.gov.verifiablelog.VerifiableLog;
import uk.gov.verifiablelog.store.memoization.MemoizationStore;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

    private VerifiableLog createVerifiableLog(EntryQueryDAO entryDAO) throws NoSuchAlgorithmException {
        return new VerifiableLog(MessageDigest.getInstance("SHA-256"), new EntryMerkleLeafStore(entryDAO), memoizationStore);
    }

    private String bytesToString(byte[] bytes) {
        return DatatypeConverter.printHexBinary(bytes).toLowerCase();
    }
}
