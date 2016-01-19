package uk.gov.indexer.fetchers;

import uk.gov.indexer.dao.SourceDBQueryDAO;

public class PGFetcher implements Fetcher {
    private SourceDBQueryDAO sourceDBQueryDAO;

    public PGFetcher(SourceDBQueryDAO sourceDBQueryDAO) {
        this.sourceDBQueryDAO = sourceDBQueryDAO;
    }

    @Override
    public FetchResult fetch(int from) {
        return new FetchResult(sourceDBQueryDAO.read(from));
    }
}
