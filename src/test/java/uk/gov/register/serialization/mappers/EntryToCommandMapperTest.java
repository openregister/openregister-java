package uk.gov.register.serialization.mappers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import uk.gov.register.core.Entry;
import uk.gov.register.core.EntryType;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.serialization.RegisterCommand;
import uk.gov.register.util.HashValue;

import java.time.Instant;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

public class EntryToCommandMapperTest {
    private EntryToCommandMapper sutMapper;

    @Before
    public void setUp() throws Exception {
        sutMapper = new EntryToCommandMapper();
    }

    @Test
    public void apply_returnsAppendEntryCommandForEntry() {
        Entry entryToMap = new Entry(1, new HashValue(HashingAlgorithm.SHA256, "item-sha"), Instant.parse("2016-07-24T16:55:00Z"), "entry1-field-1-value", EntryType.user);

        RegisterCommand mapResult = sutMapper.apply(entryToMap);

        assertThat(mapResult.getCommandName(), equalTo("append-entry"));
        assertThat(mapResult.getCommandArguments(), equalTo(Arrays.asList("user", "entry1-field-1-value", "2016-07-24T16:55:00Z", "sha-256:item-sha")));
    }
}
