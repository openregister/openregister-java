package uk.gov.register.functional;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import uk.gov.register.functional.app.RegisterRule;
import uk.gov.register.functional.app.TestRegister;
import uk.gov.register.functional.helpers.RsfComparisonHelper;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.register.functional.helpers.RecordJsonComparisonHelper.assertJsonEqual;

@RunWith(Parameterized.class)
public class IndexFunctionalTest {
    private final String testDirectory;
    private final String expectedJsonFile;
    
    @ClassRule
    public static RegisterRule register = new RegisterRule();
    public static RsfComparisonHelper rsfComparisonHelper = new RsfComparisonHelper();

    public IndexFunctionalTest(String testDirectory, String expectedJsonFile) {
        this.testDirectory = testDirectory;
        this.expectedJsonFile = expectedJsonFile;
    }
    
    @Before
    public void setup() throws IOException {
        System.setProperty("multi-item-entries-enabled", "true");
        String metadataRsf = new String(Files.readAllBytes(Paths.get("src/test/resources/fixtures/local-authority-eng-metadata.rsf")));
        String inputRsf = new String(Files.readAllBytes(Paths.get("src/test/resources/fixtures/" + testDirectory, "input.rsf")));
        
        register.wipe();
        register.loadRsf(TestRegister.local_authority_eng, metadataRsf);
        register.loadRsf(TestRegister.local_authority_eng, inputRsf);
    }

    @After
    public void teardown() {
        System.clearProperty("multi-item-entries-enabled");
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data(){
        return Arrays.asList(new Object[][] {
                { "example1", "5.json" },
                { "example2", "4.json" },
                { "example3", "3.json" }
        });
    }

    @Test
    public void shouldAllowDownloadOfIndexAsRsf() throws IOException {
        String expectedIndexRsf = new String(Files.readAllBytes(Paths.get("src/test/resources/fixtures/" + testDirectory, "index.rsf")));

        Response indexRsfResponse = register.getRequest(TestRegister.local_authority_eng, "/index/local-authority-by-type/download-rsf");
        assertThat(indexRsfResponse.getStatus(), is(200));
        String actualIndexRsf = indexRsfResponse.readEntity(String.class);

        rsfComparisonHelper.assertRsfEqual(actualIndexRsf, expectedIndexRsf);
    }

    @Test
    public void shouldDisplyIndexRecordsAsJson() throws IOException {
        String expectedRecords = new String(Files.readAllBytes(Paths.get("src/test/resources/fixtures/"+ testDirectory +"/records", expectedJsonFile)));

        Response indexJsonResponse = register.getRequest(TestRegister.local_authority_eng, "/index/local-authority-by-type/records.json");
        assertThat(indexJsonResponse.getStatus(), is(200));
        String actualIndexJson = indexJsonResponse.readEntity(String.class);

        assertJsonEqual(actualIndexJson, expectedRecords);
    }
}
