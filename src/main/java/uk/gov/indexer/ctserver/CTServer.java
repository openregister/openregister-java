package uk.gov.indexer.ctserver;

import com.amazonaws.util.json.Jackson;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;

public class CTServer {
    private final ObjectMapper objectMapper = Jackson.getObjectMapper();

    private String sthLocation;

    public CTServer(String sthLocation) {
        this.sthLocation = sthLocation;
    }

    public SignedTreeHead getSignedTreeHead() {
        return getResponse(SignedTreeHead.class, "/ct/v1/get-sth");
    }

    public Entries getEntries(int from, int to) {
        return getResponse(Entries.class, "/ct/v1/get-entries", "start", from, "end", to);
    }

    private <T> T getResponse(Class<T> responseType, String requestPath, Object... queryParamsAndValue) {
        WebTarget wt = ClientBuilder.newClient().target(sthLocation).path(requestPath);

        for (int i = 0; i < queryParamsAndValue.length; i = i + 2) {
            wt = wt.queryParam(queryParamsAndValue[i].toString(), queryParamsAndValue[i + 1]);
        }

        Response r = wt.request().get();
        try {
            if (r.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                throw new RuntimeException(String.format("%d: %s", r.getStatus(), r.getEntity()));
            }
            return readObjectFromStream(responseType, r.readEntity(InputStream.class));
        } finally {
            r.close();
        }
    }

    private <T> T readObjectFromStream(Class<T> responseType, InputStream inputStream) {
        try {
            return objectMapper.readValue(inputStream, responseType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
