package uk.gov.register.functional;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.register.functional.app.RegisterRule;
import uk.gov.register.functional.app.TestRegister;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class IndexFunctionalTest3 {
    @ClassRule
    public static RegisterRule register = new RegisterRule();

    @Before
    public void setup() throws IOException {
        System.setProperty("multi-item-entries-enabled", "true");
        register.wipe();
        String inputRsf = new String(Files.readAllBytes(Paths.get("src/test/resources/fixtures/example3", "input.rsf")));
        Response r = register.loadRsf(TestRegister.local_authority_eng, inputRsf);
    }

    @Test
    public void shouldAllowDownloadOfIndexAsRsf() throws IOException {
        String expectedIndexRsf = new String(Files.readAllBytes(Paths.get("src/test/resources/fixtures/example3", "index.rsf")));

        Response indexRsfResponse = register.getRequest(TestRegister.local_authority_eng, "/index/local-authority-by-type/download-rsf");
        assertThat(indexRsfResponse.getStatus(), is(200));
        String actualIndexRsf = indexRsfResponse.readEntity(String.class);

        assertThat(actualIndexRsf, equalTo(expectedIndexRsf));
    }

    @Test
    public void shouldDisplyIndexRecordsAsJson() throws IOException {
        String expectedRecords = new String(Files.readAllBytes(Paths.get("src/test/resources/fixtures/example3/records", "3.json")));

        Response indexJsonResponse = register.getRequest(TestRegister.local_authority_eng, "/index/local-authority-by-type/records.json");
        assertThat(indexJsonResponse.getStatus(), is(200));
        String actualIndexJson = indexJsonResponse.readEntity(String.class);

        assertThat(actualIndexJson, equalTo(expectedRecords));
    } 
}
