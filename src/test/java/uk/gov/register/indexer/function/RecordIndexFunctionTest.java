package uk.gov.register.indexer.function;

import org.junit.Before;
import org.junit.Test;
import uk.gov.register.configuration.IndexFunctionConfiguration;
import uk.gov.register.core.EntryType;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.core.Register;
import uk.gov.register.indexer.IndexKeyItemPair;
import uk.gov.register.util.HashValue;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

public class RecordIndexFunctionTest{
	RecordIndexFunction func;
	Register register;

	@Before
	public void setup() {
		register = mock(Register.class);
		func = new RecordIndexFunction(IndexFunctionConfiguration.IndexNames.RECORDS);
	}

	@Test
	public void executeWithKeyAndHash_shouldReturnIndexValueItemPairByKey() throws IOException {
		HashValue itemHash = new HashValue(HashingAlgorithm.SHA256, "abc");

		Set<IndexKeyItemPair> resultSet = new HashSet<>();
		func.execute(register, EntryType.user, "LND", new HashValue(HashingAlgorithm.SHA256, "abc"), resultSet);

		assertThat(resultSet.size(), is(1));
		assertThat(resultSet, contains(new IndexKeyItemPair("LND", itemHash)));
	}
}