package uk.gov.register.presentation.resource;

import uk.gov.register.presentation.RegisterProof;
import uk.gov.register.presentation.dao.EntryDAO;
import uk.gov.register.presentation.dao.EntryMerkleLeafStore;
import uk.gov.verifiablelog.VerifiableLog;

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

    private final EntryDAO entryDAO;

    @Inject
    public VerifiableLogResource(EntryDAO entryDAO) {
        this.entryDAO = entryDAO;
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

    private VerifiableLog createVerifiableLog(EntryDAO entryDAO) throws NoSuchAlgorithmException {
        return new VerifiableLog(MessageDigest.getInstance("SHA-256"), new EntryMerkleLeafStore(entryDAO));
    }

    private String bytesToString(byte[] bytes) {
        return DatatypeConverter.printHexBinary(bytes).toLowerCase();
    }
}
