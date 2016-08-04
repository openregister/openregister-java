package uk.gov.register.resources;

import io.dropwizard.views.View;
import uk.gov.register.db.EntryMerkleLeafStore;
import uk.gov.register.db.EntryQueryDAO;
import uk.gov.register.db.RecordQueryDAO;
import uk.gov.register.views.RegisterProof;
import uk.gov.register.views.ViewFactory;
import uk.gov.register.views.representations.ExtraMediaType;
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
import java.util.Optional;

@Path("/")
public class HomePageResource {
    private final ViewFactory viewFactory;
    private final RecordQueryDAO recordDAO;
    private EntryQueryDAO entryDAO;
    private final MemoizationStore memoizationStore;


    @Inject
    public HomePageResource(ViewFactory viewFactory, RecordQueryDAO recordDAO, EntryQueryDAO entryDAO, MemoizationStore memoizationStore) {
        this.viewFactory = viewFactory;
        this.recordDAO = recordDAO;
        this.entryDAO = entryDAO;
        this.memoizationStore = memoizationStore;
    }

    @GET
    @Produces({ExtraMediaType.TEXT_HTML})
    public View home() throws NoSuchAlgorithmException {
        try {
            entryDAO.begin();
            VerifiableLog verifiableLog = createVerifiableLog(entryDAO);
            String rootHash = bytesToString(verifiableLog.currentRoot());
            RegisterProof registerProof  = new RegisterProof(rootHash);
            return viewFactory.homePageView(
                    recordDAO.getTotalRecords(),
                    entryDAO.getTotalEntries(),
                    entryDAO.getLastUpdatedTime(),
                    registerProof);

        } finally {
            entryDAO.rollback();
        }

    }

    @GET
    @Path("/robots.txt")
    @Produces(MediaType.TEXT_PLAIN)
    public String robots() {
        return "User-agent: *\n" +
                "Disallow: /\n";
    }

    private VerifiableLog createVerifiableLog(EntryQueryDAO entryDAO) throws NoSuchAlgorithmException {
        return new VerifiableLog(MessageDigest.getInstance("SHA-256"), new EntryMerkleLeafStore(entryDAO), memoizationStore);
    }

    private String bytesToString(byte[] bytes) {
        return DatatypeConverter.printHexBinary(bytes).toLowerCase();
    }
}
