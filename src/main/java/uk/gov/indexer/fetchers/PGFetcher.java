package uk.gov.indexer.fetchers;

import uk.gov.indexer.dao.Entry;
import uk.gov.indexer.dao.SourceDBQueryDAO;

import java.util.List;

public class PGFetcher implements Fetcher {
    private SourceDBQueryDAO sourceDBQueryDAO;

    public PGFetcher(SourceDBQueryDAO sourceDBQueryDAO) {
        this.sourceDBQueryDAO = sourceDBQueryDAO;
    }

    @Override
    public List<Entry> fetch(int from) {
        return sourceDBQueryDAO.read(from);
    }
}
