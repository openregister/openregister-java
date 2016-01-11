package uk.gov.indexer.fetchers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.indexer.ctserver.*;
import uk.gov.indexer.dao.Entry;

import java.util.ArrayList;
import java.util.List;

public class CTFetcher implements Fetcher {
    private final Logger LOGGER = LoggerFactory.getLogger(CTFetcher.class);
    private final CTServer ctServer;

    public CTFetcher(CTServer ctServer) {
        this.ctServer = ctServer;
    }

    @Override
    public List<Entry> fetch(int from) {
        SignedTreeHead sth = ctServer.getSignedTreeHead();
        LOGGER.debug(String.format("Current tree size: %d", sth.getTree_size()));

        if (sth.getTree_size() - 1 > from) {
            Entries ctEntries = ctServer.getEntries(from, sth.getTree_size() - 1);
            List<Entry> entriesToWrite = new ArrayList<>();
            EntryParser entryParser = new EntryParser();
            int counter = from;
            for (MerkleTreeLeaf singleEntry : ctEntries.getEntries()) {
                String signature = ctServer.createHash(singleEntry.getLeaf_input());
                entriesToWrite.add(entryParser.parse(singleEntry, signature, ++counter));
            }
            return entriesToWrite;
        }

        return new ArrayList<>();
    }
}
