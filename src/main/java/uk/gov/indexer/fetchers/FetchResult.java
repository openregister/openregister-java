package uk.gov.indexer.fetchers;

import uk.gov.indexer.ctserver.SignedTreeHead;

public class FetchResult {
    private final SignedTreeHead signedTreeHead;
    private final EntryGetter entriesFn;

    public FetchResult(SignedTreeHead signedTreeHead, EntryGetter entriesFn) {
        this.signedTreeHead = signedTreeHead;
        this.entriesFn = entriesFn;
    }

    public SignedTreeHead getSignedTreeHead() {
        return signedTreeHead;
    }

    public EntryGetter getEntriesFn() {
        return entriesFn;
    }

    public boolean hasMoreEntries(int afterLastReadSerialNumber) {
        return afterLastReadSerialNumber < (signedTreeHead.getTree_size() - 1);
    }
}

