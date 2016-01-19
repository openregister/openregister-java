package uk.gov.indexer.fetchers;

import uk.gov.indexer.ctserver.SignedTreeHead;
import uk.gov.indexer.dao.SourceDBQueryDAO;

public class PGFetcher implements Fetcher {
    private SourceDBQueryDAO sourceDBQueryDAO;

    public PGFetcher(SourceDBQueryDAO sourceDBQueryDAO) {
        this.sourceDBQueryDAO = sourceDBQueryDAO;
    }

    @Override
    public FetchResult fetch() {
        SignedTreeHead tempSignTreeHead = new SignedTreeHead(
                sourceDBQueryDAO.lastEntryID(),
                1447421303202l,
                "JATHxRF5gczvNPP1S1WuhD8jSx2bl+WoTt8bIE3YKvU=",
                "BAMARzBFAiEAkKM3aRUBKhShdCyrGLdd8lYBV52FLrwqjHa5/YuzK7ECIFTlRmNuKLqbVQv0QS8nq0pAUwgbilKOR5piBAIC8LpS"
        );

        return new FetchResult(tempSignTreeHead, sourceDBQueryDAO::read);
    }
}
