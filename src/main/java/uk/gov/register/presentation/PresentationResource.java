package uk.gov.register.presentation;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicReference;

@Path("/latest")
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class PresentationResource {
    private final AtomicReference<byte[]> currentLatest;

    public PresentationResource(AtomicReference<byte[]> currentLatest) {

        this.currentLatest = currentLatest;
    }

    @GET
    public Response get() {
        ArrayNode jsonNodes = new ArrayNode(new JsonNodeFactory(false));
        byte[] latestMessage = currentLatest.get();
        if (latestMessage == null) {
            return Response.status(503).build();
        }
        jsonNodes.add(new String(latestMessage, Charset.forName("UTF-8")));
        return Response.ok().entity(jsonNodes).build();
    }
}
