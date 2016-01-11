package uk.gov.indexer.ctserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.util.Base64;

public class CTServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(CTServer.class);

    private String sthLocation;

    public CTServer(String sthLocation) {
        this.sthLocation = sthLocation;
    }

    public SignedTreeHead getSignedTreeHead() {
        Client client = ClientBuilder.newClient();
        WebTarget wt = client.target(sthLocation)
                .path("/ct/v1/get-sth");

        Response r = wt.request().get();
        if (r.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
            throw new RuntimeException(String.format("%d: %s", r.getStatus(), r.getEntity()));
        }
        SignedTreeHead sth = r.readEntity(SignedTreeHead.class);
        return sth;
    }

    public Entries getEntries(int from, int to) {
        LOGGER.info(String.format("Retrieving entries from %d to %d", from, to));

        Client client = ClientBuilder.newClient();
        WebTarget wt = client.target(sthLocation)
                .path("/ct/v1/get-entries")
                .queryParam("start", from)
                .queryParam("end", to);

        Response r = wt.request().get();
        if (r.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
            throw new RuntimeException(String.format("%d: %s", r.getStatus(), r.getEntity()));
        }
        Entries entries = r.readEntity(Entries.class);
        return entries;
    }

    public AuditProofs getProofByHash(int treeSize, String hash) {
        Client client = ClientBuilder.newClient();
        WebTarget wt = client.target(sthLocation)
                .path("/ct/v1/get-proof-by-hash")
                .queryParam("tree_size", treeSize)
                .queryParam("hash", hash);

        Response r = wt.request().get();
        if (r.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
            throw new RuntimeException(String.format("%d: %s", r.getStatus(), r.getEntity()));
        }

        return r.readEntity(AuditProofs.class);
    }

    public String createHash(String leaf_data) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(0x00); // Null byte prefix (http://tools.ietf.org/html/rfc6962#section-2.1)
            baos.write(Base64.getDecoder().decode(leaf_data));
            byte[] rawDataToSign = baos.toByteArray();

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(rawDataToSign);
            byte[] signature = md.digest();
            String encodedSignature = Base64.getEncoder().encodeToString(signature);
            return encodedSignature;
        } catch (Exception e) {
            LOGGER.error(e.toString());
        }
        return null;
    }

}
