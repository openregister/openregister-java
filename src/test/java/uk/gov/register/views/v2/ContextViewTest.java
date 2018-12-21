package uk.gov.register.views.v2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.jackson.Jackson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.register.core.Cardinality;
import uk.gov.register.core.Entry;
import uk.gov.register.core.EntryType;
import uk.gov.register.core.Field;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.core.Register;
import uk.gov.register.core.RegisterContext;
import uk.gov.register.core.RegisterId;
import uk.gov.register.core.RegisterMetadata;
import uk.gov.register.util.HashValue;

import java.time.Instant;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ContextViewTest {
    @Mock
    RegisterContext registerContext;

    @Mock
    Register register;

    private final ObjectMapper objectMapper = Jackson.newObjectMapper();
    private RegisterId registerId;

    @Before
    public void setup() {
        registerId = new RegisterId("test-register");

        when(registerContext.buildOnDemandRegister()).thenReturn(register);
        when(register.getRegisterId()).thenReturn(registerId);

        String rootHash = "1220e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
        when(registerContext.withVerifiableLog(any())).thenReturn(rootHash);

        when(register.getFieldsByName()).thenReturn(ImmutableMap.of(
                "field-1", new Field("field-1", "string", registerId, Cardinality.ONE, "the first field"),
                "field-2", new Field("field-2", "string", registerId, Cardinality.ONE, "the second field")
        ));
    }

    @Test
    public void emptyRegisterContext() throws JsonProcessingException {
        RegisterMetadata registerMetadata = new RegisterMetadata(
                registerId,
                ImmutableList.of("field-1","field-2"),
                null,
                null,
                null,
                "alpha");

        when(register.getRegisterMetadata()).thenReturn(registerMetadata);

        ContextView view = new ContextView(registerContext);
        String result = objectMapper.writeValueAsString(view);

        assertThat(result, equalTo("{" +
                "\"id\":\"test-register\"," +
                "\"hashing-algorithm\":{\"id\":\"sha2-256\",\"function-type\":18,\"digest-length\":32}," +
                "\"root-hash\":\"1220e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855\"," +
                "\"schema\":[{\"id\":\"field-1\",\"datatype\":\"string\",\"cardinality\":\"1\"},{\"id\":\"field-2\",\"datatype\":\"string\",\"cardinality\":\"1\"}]," +
                "\"statistics\":{\"total-records\":0,\"total-entries\":0,\"total-blobs\":0}," +
                "\"status\":{}" +
                "}"));
    }

    @Test
    public void fullRegisterContext() throws JsonProcessingException {
        RegisterMetadata registerMetadata = new RegisterMetadata(
                registerId,
                ImmutableList.of("field-1","field-2"),
                "Copyright",
                null,
                "This is a test",
                "alpha");

        when(register.getRegisterMetadata()).thenReturn(registerMetadata);

        when(register.getRegisterName()).thenReturn(Optional.of("Test register"));
        when(register.getCustodianName()).thenReturn(Optional.of("GDS"));

        Entry entry = new Entry(
                1,
                new HashValue(HashingAlgorithm.SHA256, "sha-256:abc"),
                new HashValue(HashingAlgorithm.SHA256, "1220abc"),
                Instant.EPOCH,
                "abc",
                EntryType.system
        );
        when(register.getEntry(1)).thenReturn(Optional.of(entry));

        ContextView view = new ContextView(registerContext);
        String result = objectMapper.writeValueAsString(view);

        assertThat(result, equalTo("{" +
                "\"id\":\"test-register\"," +
                "\"copyright\":\"Copyright\"," +
                "\"custodian\":\"GDS\"," +
                "\"title\":\"Test register\"," +
                "\"description\":\"This is a test\"," +
                "\"hashing-algorithm\":{\"id\":\"sha2-256\",\"function-type\":18,\"digest-length\":32}," +
                "\"root-hash\":\"1220e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855\"," +
                "\"schema\":[{\"id\":\"field-1\",\"datatype\":\"string\",\"cardinality\":\"1\"},{\"id\":\"field-2\",\"datatype\":\"string\",\"cardinality\":\"1\"}]," +
                "\"statistics\":{\"total-records\":0,\"total-entries\":0,\"total-blobs\":0}," +
                "\"status\":{\"start-date\":\"1970-01-01T00:00:00Z\"}" +
                "}"));
    }
}
