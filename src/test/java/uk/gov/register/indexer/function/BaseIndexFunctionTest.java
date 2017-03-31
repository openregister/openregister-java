package uk.gov.register.indexer.function;

import org.junit.Test;
import org.mockito.Mockito;
import uk.gov.register.core.Entry;
import uk.gov.register.indexer.IndexKeyItemPair;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

public class BaseIndexFunctionTest {
    @Test
    public void executeWithEntry_shouldReturnEmptyResultSet_whenEntryContainsNoItems() {
        Entry entry = new Entry(1, Collections.emptyList(), Instant.now(), "key");

        BaseIndexFunction func = mock(BaseIndexFunction.class, Mockito.CALLS_REAL_METHODS);
        Set<IndexKeyItemPair> resultSet = new HashSet<>();
        func.execute(entry);

        assertThat(resultSet, is(empty()));
    }
}
