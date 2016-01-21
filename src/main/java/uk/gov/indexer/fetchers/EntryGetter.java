package uk.gov.indexer.fetchers;

import uk.gov.indexer.dao.Entry;

import java.util.List;

public interface EntryGetter {
    List<Entry> get(int from);
}
