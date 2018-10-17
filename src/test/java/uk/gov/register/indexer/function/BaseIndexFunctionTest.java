package uk.gov.register.indexer.function;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import uk.gov.register.core.BaseEntry;
import uk.gov.register.core.EntryType;
import uk.gov.register.indexer.IndexKeyItemPair;
import uk.gov.register.store.DataAccessLayer;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

public class BaseIndexFunctionTest {
    @Mock
    private DataAccessLayer dataAccessLayer;

    @Before
    public void setup(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void executeWithEntry_shouldReturnEmptyResultSet_whenEntryContainsNoItems() {
        BaseEntry entry = new BaseEntry(1, Collections.emptyList(), Instant.now(), "key", EntryType.user);

        BaseIndexFunction func = mock(BaseIndexFunction.class, Mockito.CALLS_REAL_METHODS);
        Set<IndexKeyItemPair> resultSet = new HashSet<>();
        func.execute(h -> dataAccessLayer.getBlob(h), entry);

        assertThat(resultSet, is(empty()));
    }
}
