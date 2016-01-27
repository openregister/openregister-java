package uk.gov.indexer.ctserver;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.MediaType;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(MockitoJUnitRunner.class)
public class CTServerTest {
    CTServer ctServer = new CTServer("http://localhost:8090");

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8090);

    @Test(expected = RuntimeException.class)
    public void exceptionPropagatesForGetSth() {
        stubFor(get(urlEqualTo("/ct/v1/get-sth"))
                .willReturn(aResponse()
                                .withStatus(500)
                                .withBody("Some simulated error")
                ));

        CTServer objectUnderTest = new CTServer("http://localhost:8090");
        objectUnderTest.getSignedTreeHead();
        fail("Should have thrown an exception");
    }

    @Test(expected = RuntimeException.class)
    public void exceptionPropagatesForGetEntries() {
        stubFor(get(urlEqualTo("/ct/v1/get-entries"))
                .withQueryParam("start", equalTo("0"))
                .withQueryParam("end", equalTo("999"))
                .willReturn(aResponse()
                                .withStatus(500)
                                .withBody("Some simulated error")
                ));


        ctServer.getEntries(0, 999);
        fail("Should have thrown an exception");
    }

    @Test
    public void getSignedTreeHead_returnsTheSignedTreeHeadOfTheTree() {
        stubFor(get(urlEqualTo("/ct/v1/get-sth"))
                .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON)
                                .withBody("{ " +
                                        "\"tree_size\": 5788, " +
                                        "\"timestamp\": 1452868835667, " +
                                        "\"sha256_root_hash\": \"sha256roothash\", " +
                                        "\"tree_head_signature\": \"treeHEadSignature\" " +
                                        "}")
                ));

        SignedTreeHead signedTreeHead = ctServer.getSignedTreeHead();

        assertThat(signedTreeHead.getTimestamp(), is(1452868835667l));
        assertThat(signedTreeHead.getTree_size(), is(5788));

    }

    @Test
    public void getEntries_returnsTheListOfEntriesInMerkleTree() {
        String leaf_input1 = "AAAAAAFSeasJ5IAAAABZeyAib3duZXIiOiAiRm9yZXN0cnkgQ29tbWlzc2lvbiIsICJlbmQtZGF0ZSI6ICIiLCAiZ292ZXJubWVudC1kb21haW4iOiAiN3N0YW5lcy5nb3YudWsiIH0AAA==";
        String leaf_input2 = "AAAAAAFSeasJnoAAAABqeyAib3duZXIiOiAiNHAncyBQdWJsaWMgUHJpdmF0ZSBQYXJ0bmVyc2hpcHMgUHJvZ3JhbSIsICJlbmQtZGF0ZSI6ICIiLCAiZ292ZXJubWVudC1kb21haW4iOiAiNHBzLmdvdi51ayIgfQAA";

        stubFor(get(urlEqualTo("/ct/v1/get-entries?start=2&end=3"))
                .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON)
                                .withBody("{ \"entries\": " +
                                        "[ " +
                                        "{ \"leaf_input\": \"" + leaf_input1 + "\", \"extra_data\": \"\" }, " +
                                        "{ \"leaf_input\": \"" + leaf_input2 + "\", \"extra_data\": \"\" } " +
                                        "] " +
                                        "}")
                ));

        List<CTEntryLeaf> entries = ctServer.getEntries(2, 3).entries;

        assertThat(entries.size(), is(2));

        assertThat(entries.get(0).getLeafInput(), is(leaf_input1));
        assertThat(entries.get(1).getLeafInput(), is(leaf_input2));

    }
}
