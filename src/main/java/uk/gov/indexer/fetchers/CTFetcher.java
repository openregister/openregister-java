package uk.gov.indexer.fetchers;

import uk.gov.indexer.ctserver.CTServer;
import uk.gov.indexer.ctserver.EntryParser;
import uk.gov.indexer.ctserver.SignedTreeHead;
import uk.gov.indexer.dao.Entry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class CTFetcher implements Fetcher {
    private final CTServer ctServer;

    private final EntryParser entryParser = new EntryParser();

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
                            .map(treeLeaf -> entryParser.parse(treeLeaf, atomicInteger.incrementAndGet()))
                            .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}
