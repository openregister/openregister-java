package uk.gov.indexer.fetchers;

import uk.gov.indexer.ctserver.SignedTreeHead;

public class FetchResult {
    private final SignedTreeHead signedTreeHead;
    private final EntryFetcher entryFetcher;

    public FetchResult(SignedTreeHead signedTreeHead, EntryFetcher entryFetcher) {
        this.signedTreeHead = signedTreeHead;
        this.entryFetcher = entryFetcher;
    }

    public SignedTreeHead getSignedTreeHead() {
        return signedTreeHead;
    }

    public EntryFetcher getEntryFetcher() {
        return entryFetcher;
    }

    public boolean hasMoreEntries(int afterLastReadSerialNumber) {
        return afterLastReadSerialNumber < (signedTreeHead.getTree_size() - 1);
    }
}

