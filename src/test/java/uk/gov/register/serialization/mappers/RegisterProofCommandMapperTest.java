package uk.gov.register.serialization.mappers;

import org.junit.Before;
import org.junit.Test;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.serialization.RegisterCommand;
import uk.gov.register.util.HashValue;
import uk.gov.register.views.RegisterProof;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class RegisterProofCommandMapperTest {
    private RegisterProofCommandMapper sutMapper;

    @Before
    public void setUp() throws Exception {
        sutMapper = new RegisterProofCommandMapper();
    }

    @Test
    public void apply_returnsAssertRootHashCommandForRegisterProof() {
        RegisterProof emptyRegisterProof = new RegisterProof(new HashValue(HashingAlgorithm.SHA256, "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"));

        RegisterCommand mapResult = sutMapper.apply(emptyRegisterProof);

        assertThat(mapResult.getCommandName(), equalTo("assert-root-hash"));
        assertThat(mapResult.getCommandArguments(), equalTo(Collections.singletonList("sha-256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")));
    }
}