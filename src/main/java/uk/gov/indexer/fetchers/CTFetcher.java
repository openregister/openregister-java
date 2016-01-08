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

        // Step 1 - get the latest entries from the CT server
        SignedTreeHead sth = ctServer.getSignedTreeHead();
        LOGGER.info(String.format("Current tree size: %d", sth.getTree_size()));

        if(sth.getTree_size()-1 > from) {
            // Step 2 - read from lastRead to TreeSize entries
            //Entries ctEntries = ctServer.getEntries(lastRead+1, sth.getTree_size()-1);
            Entries ctEntries = ctServer.getEntries(from, sth.getTree_size()-1);

            // Step 3 - convert to something we can use
            List<Entry> entriesToWrite = new ArrayList<>();
            EntryParser entryParser = new EntryParser();
            int counter = from;
            for(MerkleTreeLeaf singleEntry : ctEntries.getEntries()) {
                entriesToWrite.add(entryParser.parse(singleEntry, ++counter));
            }
            return entriesToWrite;
        }

        return new ArrayList<>();
    }
}
