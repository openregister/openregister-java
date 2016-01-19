package uk.gov.indexer.fetchers;

import uk.gov.indexer.ctserver.SignedTreeHead;
import uk.gov.indexer.dao.Entry;

import java.util.List;

public class FetchResult {
    private final SignedTreeHead signedTreeHead;
    private final List<Entry> entries;

    public FetchResult(SignedTreeHead signedTreeHead, List<Entry> entries) {
        this.signedTreeHead = signedTreeHead;
        this.entries = entries;
    }

    //this constructor is temporary and used by PGFetcher, this will be removed when we move on ct server fully
    public FetchResult(List<Entry> entries) {
        this(
                new SignedTreeHead(
                        9803348,
                        1447421303202l,
                        "JATHxRF5gczvNPP1S1WuhD8jSx2bl+WoTt8bIE3YKvU=",
                        "BAMARzBFAiEAkKM3aRUBKhShdCyrGLdd8lYBV52FLrwqjHa5/YuzK7ECIFTlRmNuKLqbVQv0QS8nq0pAUwgbilKOR5piBAIC8LpS"
                ),
                entries
        );
    }

    public SignedTreeHead getSignedTreeHead() {
        return signedTreeHead;
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public boolean hasEntries() {
        return entries.size() > 0;
    }
}
