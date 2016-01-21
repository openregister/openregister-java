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
public class CTFetcherTest {
    @Mock
    CTServer ctServer;

    @Test
    public void fetch_fetchesListOfEntriesIfAvailable() {
        CTFetcher ctFetcher = new CTFetcher(ctServer);

        SignedTreeHead signedTreeHead = new SignedTreeHead(12, 123l, "", "");

        when(ctServer.getSignedTreeHead()).thenReturn(signedTreeHead);

        Entries entries = new Entries();
        entries.entries = new ArrayList<>();
        MerkleTreeLeaf leaf1 = new MerkleTreeLeaf();
        leaf1.leaf_input = "AAAAAAFSRUq/EIAAAABieyAib3duZXIiOiAiVmV0ZXJpbmFyeSBNZWRpY2luZXMgRGlyZWN0b3JhdGUiLCAiZW5kLWRhdGUiOiAiIiwgImdvdmVybm1lbnQtZG9tYWluIjogImFtaS5nb3YudWsiIH0AAA==";
        entries.entries.add(leaf1);

        MerkleTreeLeaf leaf2 = new MerkleTreeLeaf();
        leaf2.leaf_input = "AAAAAAFSRUq/VYAAAABdeyAib3duZXIiOiAiQW5kb3ZlciBUb3duIENvdW5jaWwiLCAiZW5kLWRhdGUiOiAiIiwgImdvdmVybm1lbnQtZG9tYWluIjogImFuZG92ZXItdGMuZ292LnVrIiB9AAA=";
        entries.entries.add(leaf2);
        when(ctServer.getEntries(10, 11)).thenReturn(entries);

        List<Entry> result = ctFetcher.fetch().getEntriesFn().get(10);

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

        CTFetcher ctFetcher = new CTFetcher(ctServer);

        SignedTreeHead signedTreeHead = new SignedTreeHead(10, 123l, "", "");

        when(ctServer.getSignedTreeHead()).thenReturn(signedTreeHead);

        FetchResult fetchResult = ctFetcher.fetch();

        assertThat(fetchResult.getEntriesFn().get(10).size(), equalTo(0));

    }
}
