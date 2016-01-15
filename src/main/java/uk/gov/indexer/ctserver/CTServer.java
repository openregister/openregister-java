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
        return r.readEntity(SignedTreeHead.class);

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
        return r.readEntity(Entries.class);
    }
}
