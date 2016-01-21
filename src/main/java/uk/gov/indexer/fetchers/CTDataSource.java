package uk.gov.indexer.fetchers;

import uk.gov.indexer.JsonUtils;
import uk.gov.indexer.ctserver.*;
import uk.gov.indexer.dao.Entry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class CTDataSource implements DataSource {
    private final CTServer ctServer;

    public CTDataSource(CTServer ctServer) {
        this.ctServer = ctServer;
    }

    @Override
    public FetchResult fetchCurrentSnapshot() {
        SignedTreeHead sth = ctServer.getSignedTreeHead();

        EntryFetcher entryFetcher = (from) -> {
            int lastEntryNumber = sth.getTree_size();

            if (from < lastEntryNumber) {

                AtomicInteger atomicInteger = new AtomicInteger(from);

                return ctServer.getEntries(from, lastEntryNumber - 1 )
                        .entries
                        .stream()
                        .map(treeLeaf -> new CTEntryLeaf(treeLeaf.leaf_input).payload)
                        .map(payload -> createEntry(atomicInteger.incrementAndGet(), payload))
                        .collect(Collectors.toList());
            }
            return new ArrayList<>();
        };

        return new FetchResult(sth, entryFetcher);
    }

    private Entry createEntry(int serialNumber, byte[] payload) {
        Map<String, Object> object = new HashMap<>();

        object.put("hash", SHA256Hash.createHash(payload));
        object.put("entry", JsonUtils.fromBytesToJsonNode(payload));

        return new Entry(serialNumber, JsonUtils.toBytes(object));
    }


}
