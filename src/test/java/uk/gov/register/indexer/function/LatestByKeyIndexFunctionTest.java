package uk.gov.register.indexer.function;

import org.junit.Before;
import org.junit.Test;
import uk.gov.register.configuration.IndexFunctionConfiguration;
import uk.gov.register.core.EntryType;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.indexer.IndexKeyItemPair;
import uk.gov.register.store.DataAccessLayer;
import uk.gov.register.util.HashValue;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

public class LatestByKeyIndexFunctionTest {
	LatestByKeyIndexFunction func;
	DataAccessLayer dataAccessLayer;

	@Before
	public void setup() {
		dataAccessLayer = mock(DataAccessLayer.class);
		func = new LatestByKeyIndexFunction(IndexFunctionConfiguration.IndexNames.RECORD);
	}

	@Test
	public void executeWithKeyAndHash_shouldReturnIndexValueItemPairByKey() throws IOException {
		HashValue itemHash = new HashValue(HashingAlgorithm.SHA256, "abc");

		Set<IndexKeyItemPair> resultSet = new HashSet<>();
		func.execute(h -> dataAccessLayer.getItem(h), EntryType.user, "LND", new HashValue(HashingAlgorithm.SHA256, "abc"), resultSet);

		assertThat(resultSet.size(), is(1));
		assertThat(resultSet, contains(new IndexKeyItemPair("LND", itemHash)));
	}
}
