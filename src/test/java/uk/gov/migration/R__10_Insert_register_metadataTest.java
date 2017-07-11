package uk.gov.migration;

import org.junit.Test;
import uk.gov.register.core.Record;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class R__10_Insert_register_metadataTest {
    @Test
    public void parseRegisterYaml() throws Exception {
        R__10_Insert_register_metadata migration = new R__10_Insert_register_metadata();
        InputStream inputStream = Files.newInputStream(Paths.get("src/test/resources/config", "external-registers.yaml"));
        List<Record> records = migration.parseYamlToRecords(Arrays.asList("country"), inputStream, "register");
        assertThat(records.get(0).getItems().get(0).getValue("register").get(), is("country"));
    }

    @Test
    public void parseFieldsYaml() throws Exception {
        R__10_Insert_register_metadata migration = new R__10_Insert_register_metadata();
        InputStream inputStream = Files.newInputStream(Paths.get("src/test/resources/config", "external-fields.yaml"));
        List<Record> fieldRecords = migration.parseYamlToRecords(Arrays.asList("country"), inputStream, "field");
        assertThat(fieldRecords.size(), is(1));
    }

}
