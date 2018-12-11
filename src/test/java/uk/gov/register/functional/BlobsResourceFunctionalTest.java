package uk.gov.register.functional;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.register.functional.app.RegisterRule;
import uk.gov.register.functional.app.RsfRegisterDefinition;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static uk.gov.register.functional.app.TestRegister.address;

public class BlobsResourceFunctionalTest {
    @ClassRule
    public static RegisterRule register = new RegisterRule();
    private final WebTarget addressTarget = register.target(address);

    @Before
    public void setup() {
        register.wipe();
        register.loadRsf(address, RsfRegisterDefinition.ADDRESS_FIELDS + RsfRegisterDefinition.ADDRESS_REGISTER);

        String rsf = "add-item\t{\"address\":\"6789\",\"street\":\"presley\"}\n" +
                "add-item\t{\"address\":\"145678\",\"street\":\"ellis\"}\n" +
                "add-item\t{\"address\":\"567\",\"street\":\"john\"}\n" +
                "append-entry\tuser\tregister1\t2018-07-26T15:48:03Z\tsha-256:bd239db51960376826b937a615f0f3397485f00611d35bb7e951e357bf73b934\n" +
                "append-entry\tuser\tregister1\t2018-07-26T15:48:03Z\tsha-256:8ac926428ee49fb83c02bdd2556e62e84cfd9e636cd35eb1306ac8cb661e4983\n" +
                "append-entry\tuser\tregister1\t2018-07-26T15:48:03Z\tsha-256:6352a25fe8c9222676b29ba6f5e675b5dfa2b886010a844dd596b0d0cd615849\n";

        register.loadRsf(address, rsf);
    }

    @Test
    public void hasNextHeaderIfMorePages() {
        Response response = addressTarget.path("/next/blobs.json")
                .queryParam("limit", 2)
                .request().get();
        ArrayNode jsonNodes = response.readEntity(ArrayNode.class);

        assertThat(jsonNodes.size(), equalTo(2));
        assertTrue(response.getStringHeaders().containsKey("Link"));
    }

    @Test
    public void nextHeaderCanBeUsedToPaginate(){
        Response response = addressTarget.path("/next/blobs.json")
                .queryParam("limit", 2)
                .request().get();
        String link = response.getHeaderString("Link");
        String newStart = extractStartParam(link);

        response = addressTarget.path("/next/blobs.json")
                .queryParam("start", newStart)
                .queryParam("limit", 2)
                .request().get();
        ArrayNode jsonNodes = response.readEntity(ArrayNode.class);

        assertThat(jsonNodes.size(), equalTo(1));
        assertFalse(hasNextPage(response));
    }

    @Test
    public void noNextHeaderIfFitsOnOnePage() {
        Response response = addressTarget.path("/next/blobs.json")
                .queryParam("limit", 3)
                .request().get();
        ArrayNode jsonNodes = response.readEntity(ArrayNode.class);

        assertThat(jsonNodes.size(), equalTo(3));
        assertFalse(hasNextPage(response));
    }

    @Test
    public void orderingIsStableIfMoreItemsAreAdded() {
        Response response = addressTarget.path("/next/blobs.json")
                .queryParam("limit", 3)
                .request().get();
        ArrayNode firstRead = response.readEntity(ArrayNode.class);


        String rsf = "add-item\t{\"address\":\"6789\",\"street\":\"presley2\"}\n" +
                "add-item\t{\"address\":\"145678\",\"street\":\"ellis2\"}\n" +
                "add-item\t{\"address\":\"567\",\"street\":\"john2\"}\n" +
                "append-entry\tuser\tregister1\t2018-07-26T15:48:03Z\tsha-256:a2d23f647d0793dfe3deb6bc56e8711ed279ac96b7e76a65cc7b4ea4cfb36ab3\n" +
                "append-entry\tuser\tregister1\t2018-07-26T15:48:03Z\tsha-256:c282e87794f32a304fa889e72e1fbcad542cf950b64f37a28bee325a4e826862\n" +
                "append-entry\tuser\tregister1\t2018-07-26T15:48:03Z\tsha-256:89b3c20d8fb49b41e413fe56e0bbad456e47d0fe3ec9a2c7b44f7f3bfc0c362f\n";

        register.loadRsf(address, rsf);

        response = addressTarget.path("/next/blobs.json")
                .queryParam("limit", 3)
                .request().get();
        ArrayNode secondRead = response.readEntity(ArrayNode.class);

        assertEquals(firstRead, secondRead);
        assertTrue(hasNextPage(response));
    }

    private boolean hasNextPage(Response response) {
        return response.getStringHeaders().containsKey("Link");
    }

    private String extractStartParam(String linkHeader) {
        return linkHeader.replaceAll("<\\?start=([a-f0-9]+)&limit=2>; rel=\"next\"", "$1");
    }
}
