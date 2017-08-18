package uk.gov.register.functional;

import com.google.common.collect.ImmutableMap;
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
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.register.functional.helpers.RecordJsonComparisonHelper.assertJsonEqual;

@RunWith(Parameterized.class)
public class IndexFunctionalTest {
    
    @ClassRule
    public static RegisterRule register = new RegisterRule();
    public static RsfComparisonHelper rsfComparisonHelper = new RsfComparisonHelper();
    private final Map<String, String> inputAndExpectedData;

    public IndexFunctionalTest(Map<String, String> inputAndExpectedData) {
        this.inputAndExpectedData = inputAndExpectedData;
    }
    
    @Before
    public void setup() throws IOException {
        System.setProperty("multi-item-entries-enabled", "true");
        String metadataRsf = new String(Files.readAllBytes(Paths.get("src/test/resources/fixtures/local-authority-eng-metadata.rsf")));
        
        register.wipe();
        register.loadRsf(TestRegister.local_authority_eng, metadataRsf);
    }

    @After
    public void teardown() {
        System.clearProperty("multi-item-entries-enabled");
    }
    
    @Parameterized.Parameters
    public static Collection<Map<String, String>> data(){
        return Arrays.asList(
                ImmutableMap.of("example1", "5.json"),
                ImmutableMap.of("example2", "4.json"),
                ImmutableMap.of("example3", "3.json"),
                ImmutableMap.of("example4/1", "1.json", "example4/2", "2.json"),
                ImmutableMap.of("example5/1", "2.json", "example5/2", "3.json")
        );
    }

    @Test
    public void shouldAllowDownloadOfIndexAsRsf() throws IOException {
        for (String testDirectory : inputAndExpectedData.keySet()) {
            String rsfInput = new String(Files.readAllBytes(Paths.get("src/test/resources/fixtures/" + testDirectory, "input.rsf")));
            register.loadRsf(TestRegister.local_authority_eng, rsfInput);
            
            String expectedIndexRsf = new String(Files.readAllBytes(Paths.get("src/test/resources/fixtures/" + testDirectory, "index.rsf")));
            Response rsfInputResponse = register.getRequest(TestRegister.local_authority_eng, "/index/local-authority-by-type/download-rsf");
            assertThat(rsfInputResponse.getStatus(), is(200));
            String actualIndexRsf = rsfInputResponse.readEntity(String.class);
            rsfComparisonHelper.assertRsfEqual(expectedIndexRsf, actualIndexRsf);
        }
    }

    @Test
    public void shouldDisplyIndexRecordsAsJson() throws IOException {
        for (String testDirectory : inputAndExpectedData.keySet()) {
            String rsfInput = new String(Files.readAllBytes(Paths.get("src/test/resources/fixtures/" + testDirectory, "input.rsf")));
            register.loadRsf(TestRegister.local_authority_eng, rsfInput);
            
            String expectedJsonFile = inputAndExpectedData.get(testDirectory);
            String expectedRecords = new String(Files.readAllBytes(Paths.get("src/test/resources/fixtures/"+ testDirectory + "/records", expectedJsonFile)));
            Response indexJsonResponse = register.getRequest(TestRegister.local_authority_eng, "/index/local-authority-by-type/records.json");
            assertThat(indexJsonResponse.getStatus(), is(200));
            String actualIndexJson1 = indexJsonResponse.readEntity(String.class);
            assertJsonEqual(expectedRecords, actualIndexJson1);
        }
    }
}
