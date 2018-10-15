package uk.gov.register.serialization.mappers;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.Before;
import org.junit.Test;
import uk.gov.register.core.Blob;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.serialization.RegisterCommand;
import uk.gov.register.util.HashValue;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class BlobToCommandMapperTest {
    private static final JsonNodeFactory jsonFactory = JsonNodeFactory.instance;
    private ItemToCommandMapper sutMapper;

    @Before
    public void setUp() throws Exception {
        sutMapper = new ItemToCommandMapper();
    }

    @Test
    public void apply_returnsAddItemCommandForItem() {
        Blob blobToMap = new Blob(new HashValue(HashingAlgorithm.SHA256, "item1sha"), jsonFactory.objectNode()
                .put("field-1", "entry1-field-1-value")
                .put("field-2", "entry1-field-2-value"));

        RegisterCommand mapResult = sutMapper.apply(blobToMap);

        assertThat(mapResult.getCommandName(), equalTo("add-item"));
        assertThat(mapResult.getCommandArguments(), equalTo(Collections.singletonList("{\"field-1\":\"entry1-field-1-value\",\"field-2\":\"entry1-field-2-value\"}")));
    }
}
