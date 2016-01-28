package uk.gov.indexer.fetchers;


import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.indexer.JsonUtils;
import uk.gov.indexer.ctserver.*;
import uk.gov.indexer.dao.Entry;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CTDataSourceTest {
    @Mock
    CTServer ctServer;

    @Test
    public void fetch_fetchesListOfEntriesIfAvailable() {
        CTDataSource ctFetcher = new CTDataSource(ctServer);

        SignedTreeHead signedTreeHead = new SignedTreeHead(12, 123l, "", "");

        when(ctServer.getSignedTreeHead()).thenReturn(signedTreeHead);

        CTEntries ctEntries = new CTEntries();
        ctEntries.entries = new ArrayList<>();
        ctEntries.entries.add(new CTEntryLeaf("AAAAAAFSRUq/EIAAAABieyAib3duZXIiOiAiVmV0ZXJpbmFyeSBNZWRpY2luZXMgRGlyZWN0b3JhdGUiLCAiZW5kLWRhdGUiOiAiIiwgImdvdmVybm1lbnQtZG9tYWluIjogImFtaS5nb3YudWsiIH0AAA=="));

        ctEntries.entries.add(new CTEntryLeaf("AAAAAAFSRUq/VYAAAABdeyAib3duZXIiOiAiQW5kb3ZlciBUb3duIENvdW5jaWwiLCAiZW5kLWRhdGUiOiAiIiwgImdvdmVybm1lbnQtZG9tYWluIjogImFuZG92ZXItdGMuZ292LnVrIiB9AAA="));
        when(ctServer.getEntries(10, 11)).thenReturn(ctEntries);

        List<Entry> result = ctFetcher.fetchCurrentSnapshot().getEntryFetcher().fetch(10);

        assertThat(result.size(), equalTo(2));

        Entry entry1 = result.get(0);
        assertThat(entry1.serial_number, equalTo(11));

        JsonNode node1 = JsonUtils.fromBytesToJsonNode(entry1.contents);
        byte[] expectedItemBytes1 = "{ \"owner\": \"Veterinary Medicines Directorate\", \"end-date\": \"\", \"government-domain\": \"ami.gov.uk\" }".getBytes();
        assertThat(node1.get("entry").toString(), equalTo(JsonUtils.fromBytesToJsonNode(expectedItemBytes1).toString()));
        assertThat(node1.get("hash").textValue(), equalTo(SHA256Hash.createHash(expectedItemBytes1)));

        Entry entry2 = result.get(1);
        assertThat(entry2.serial_number, equalTo(12));

        JsonNode node2 = JsonUtils.fromBytesToJsonNode(entry2.contents);
        byte[] expectedItemBytes2 = "{ \"owner\": \"Andover Town Council\", \"end-date\": \"\", \"government-domain\": \"andover-tc.gov.uk\" }".getBytes();
        assertThat(node2.get("entry").toString(), equalTo(JsonUtils.fromBytesToJsonNode(expectedItemBytes2).toString()));
        assertThat(node2.get("hash").textValue(), equalTo(SHA256Hash.createHash(expectedItemBytes2)));
    }

    @Test
    public void fetch_returnsEmptyListIfNoEntryAvailable() {

        CTDataSource ctFetcher = new CTDataSource(ctServer);

        SignedTreeHead signedTreeHead = new SignedTreeHead(10, 123l, "", "");

        when(ctServer.getSignedTreeHead()).thenReturn(signedTreeHead);

        FetchResult fetchResult = ctFetcher.fetchCurrentSnapshot();

        assertThat(fetchResult.getEntryFetcher().fetch(10).size(), equalTo(0));

    }
}
