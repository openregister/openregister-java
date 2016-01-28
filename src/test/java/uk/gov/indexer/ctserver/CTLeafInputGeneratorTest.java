package uk.gov.indexer.ctserver;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class CTLeafInputGeneratorTest {
    @Test
    public void createLeafInputFrom_generatesTheLeafInputFromPayloadAndTimestamp() {
        String expectedLeafInput = "AAAAAAFSh+9YtoAAAAPMeyAib3duZXIiOiAiQWJlcnRpbGx5IGFuZCBMbGFuaGlsbGV0aCBDb21tdW5pdHkgQ291bmNpbCBBYmVydGlsbHkgYW5kIExsYW5oaWxsZXRoIENvbW11bml0eSBDb3VuY2lsIEFiZXJ0aWxseSBhbmQgTGxhbmhpbGxldGggQ29tbXVuaXR" +
                "5IENvdW5jaWwgQWJlcnRpbGx5IGFuZCBMbGFuaGlsbGV0aCBDb21tdW5pdHkgQ291bmNpbCBBYmVydGlsbHkgYW5kIExsYW5oaWxsZXRoIENvbW11bml0eSBDb3VuY2lsIEFiZXJ0aWxseSBhbmQgTGxhbmhpbGxldGggQ29tbXVuaXR5IENvdW5jaWwgQWJlcnRpbGx5IGFuZCBMbGFua" +
                "GlsbGV0aCBDb21tdW5pdHkgQ291bmNpbCBBYmVydGlsbHkgYW5kIExsYW5oaWxsZXRoIENvbW11bml0eSBDb3VuY2lsIEFiZXJ0aWxseSBhbmQgTGxhbmhpbGxldGggQ29tbXVuaXR5IENvdW5jaWwgQWJlcnRpbGx5IGFuZCBMbGFuaGlsbGV0aCBDb21tdW5pdHkgQ291bmN" +
                "pbCBBYmVydGlsbHkgYW5kIExsYW5oaWxsZXRoIENvbW11bml0eSBDb3VuY2lsIEFiZXJ0aWxseSBhbmQgTGxhbmhpbGxldGggQ29tbXVuaXR5IENvdW5jaWwgQWJlcnRpbGx5IGFuZCBMbGFuaGlsbGV0aCBDb21tdW5pdHkgQ291bmNpbCBBYmVydGlsbHkgYW5kIExsYW5oaWxsZXRoI" +
                "ENvbW11bml0eSBDb3VuY2lsIEFiZXJ0aWxseSBhbmQgTGxhbmhpbGxldGggQ29tbXVuaXR5IENvdW5jaWwgQWJlcnRpbGx5IGFuZCBMbGFuaGlsbGV0aCBDb21tdW5pdHkgQ291bmNpbCBBYmVydGlsbHkgYW5kIExsYW5oaWxsZXRoIENvbW11bml0eSBDb3VuY2lsIEFiZXJ0aWxseSB" +
                "hbmQgTGxhbmhpbGxldGggQ29tbXVuaXR5IENvdW5jaWwgQWJlcnRpbGx5IGFuZCBMbGFuaGlsbGV0aCBDb21tdW5pdHkgQ291bmNpbCBBYmVydGlsbHkgYW5kIExsYW5oaWxsZXRoIENvbW11bml0eSBDb3VuY2lsICIsICJlbmQtZGF0ZSI6ICIiLCAiZ292ZXJubWVudC1kb21haW4iO" +
                "iAiYWJlcnRpbGxlcnlhbmRsbGFuaGlsbGV0aC13Y2MuZ292LnVrIiB9AAA=";

        CTEntryLeaf ctEntryLeaf = new CTEntryLeaf(expectedLeafInput);

        String payload = new String(ctEntryLeaf.getPayload());

        long timestamp = ctEntryLeaf.getTimestamp();

        assertThat(ctEntryLeaf.getContentLength(), equalTo(972));

        String actualLeafInput = CTLeafInputGenerator.createLeafInputFrom(payload, timestamp);

        assertThat(actualLeafInput, equalTo(expectedLeafInput));
    }

}
