package uk.gov.indexer.fetchers;

import uk.gov.indexer.JsonUtils;
import uk.gov.indexer.ctserver.*;
import uk.gov.indexer.dao.Entry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class CTFetcher implements Fetcher {
    private final CTServer ctServer;

    public CTFetcher(CTServer ctServer) {
        this.ctServer = ctServer;
    }

    @Override
    public List<Entry> fetch(int startIndex) {
        SignedTreeHead sth = ctServer.getSignedTreeHead();

        int lastEntryIndex = sth.tree_size - 1;

        if (lastEntryIndex > startIndex) {
            AtomicInteger atomicInteger = new AtomicInteger(startIndex);
            return
                    ctServer.getEntries(startIndex, lastEntryIndex)
                            .entries
                            .stream()
                            .map(treeLeaf -> new CTEntryLeaf(treeLeaf.leaf_input).payload)
                            .map(payload -> createEntry(atomicInteger.incrementAndGet(), payload))
                            .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }

    private Entry createEntry(int serialNumber, byte[] payload) {
        Map<String, Object> object = new HashMap<>();

        object.put("hash", SHA256Hash.createHash(payload));
        object.put("entry", JsonUtils.fromBytesToJsonNode(payload));

        return new Entry(serialNumber, JsonUtils.toBytes(object));
    }


}
