package uk.gov.indexer.fetchers;

import uk.gov.indexer.dao.Entry;

import java.util.List;

public interface Fetcher {
    List<Entry> fetch(int from);
}
