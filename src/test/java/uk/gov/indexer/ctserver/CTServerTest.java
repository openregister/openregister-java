package uk.gov.indexer.ctserver;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.fail;

@RunWith(MockitoJUnitRunner.class)
public class CTServerTest {

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

        CTServer objectUnderTest = new CTServer("http://localhost:8090");
        objectUnderTest.getEntries(0, 999);
        fail("Should have thrown an exception");
    }
}
