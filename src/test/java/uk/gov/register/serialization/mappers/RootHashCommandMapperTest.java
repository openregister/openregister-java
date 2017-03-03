package uk.gov.register.serialization.mappers;

import org.junit.Before;
import org.junit.Test;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.serialization.RegisterCommand;
import uk.gov.register.util.HashValue;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class RootHashCommandMapperTest {
    private RootHashCommandMapper sutMapper;

    @Before
    public void setUp() throws Exception {
        sutMapper = new RootHashCommandMapper();
    }

    @Test
    public void apply_returnsAssertRootHashCommandForRegisterProof() {
        HashValue emptyRootHash = new HashValue(HashingAlgorithm.SHA256, "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");

        RegisterCommand mapResult = sutMapper.apply(emptyRootHash);

        assertThat(mapResult.getCommandName(), equalTo("assert-root-hash"));
        assertThat(mapResult.getCommandArguments(), equalTo(Collections.singletonList("sha-256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")));
    }
}
