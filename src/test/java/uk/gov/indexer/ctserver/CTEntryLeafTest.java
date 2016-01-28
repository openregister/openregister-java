package uk.gov.indexer.ctserver;

import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class CTEntryLeafTest {
    @Test
    public void getTimestamp_returnsTheTimestampFromLeafInput() {
        CTEntryLeaf ctEntryLeaf = new CTEntryLeaf("AAAAAAFSeasJ5IAAAABZeyAib3duZXIiOiAiRm9yZXN0cnkgQ29tbWlzc2lvbiIsICJlbmQtZGF0ZSI6ICIiLCAiZ292ZXJubWVudC1kb21haW4iOiAiN3N0YW5lcy5nb3YudWsiIH0AAA==");
        long timestamp = ctEntryLeaf.getTimestamp();
        assertThat(timestamp, equalTo(1453740198372L));
    }


    @Test
    public void getPayload_returnsTheJsonData(){
        CTEntryLeaf ctEntryLeaf = new CTEntryLeaf("AAAAAAFSeasJ5IAAAABZeyAib3duZXIiOiAiRm9yZXN0cnkgQ29tbWlzc2lvbiIsICJlbmQtZGF0ZSI6ICIiLCAiZ292ZXJubWVudC1kb21haW4iOiAiN3N0YW5lcy5nb3YudWsiIH0AAA==");
        byte[] payload = ctEntryLeaf.getPayload();
        assertThat(new String(payload), equalTo("{ \"owner\": \"Forestry Commission\", \"end-date\": \"\", \"government-domain\": \"7stanes.gov.uk\" }"));
    }

}
