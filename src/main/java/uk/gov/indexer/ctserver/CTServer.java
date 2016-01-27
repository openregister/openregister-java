package uk.gov.indexer.ctserver;

import uk.gov.indexer.JsonUtils;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.InputStream;

public class CTServer {

    private String sthLocation;

    public CTServer(String sthLocation) {
        this.sthLocation = sthLocation;
    }

    public SignedTreeHead getSignedTreeHead() {
        return getResponse(SignedTreeHead.class, "/ct/v1/get-sth");
    }

    public CTEntries getEntries(int from, int to) {
        return getResponse(CTEntries.class, "/ct/v1/get-entries", "start", from, "end", to);
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
            return JsonUtils.fromStream(r.readEntity(InputStream.class), responseType);
        } finally {
            r.close();
        }
    }
}
