package uk.gov.register.functional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public class DataDownloadFunctionalTest extends FunctionalTestBase {

    private final String item1 = "{\"street\":\"ellis\",\"address\":\"12345\"}";
    private final String item2 = "{\"street\":\"presley\",\"address\":\"6789\"}";
    private final String item3 =  "{\"street\":\"foo\",\"address\":\"12345\"}";
    private final String item4 =  "{\"street\":\"ellis\",\"address\":\"145678\"}";

    @Before
    public void publishTestMessages() {
        mintItems(item1, item2, item3, item4, item1);
    }

    @Test
    public void downloadRegister_shouldReturnAZipfile() throws IOException {
        Response response = getRequest("/download-register");

        assertThat(response.getHeaderString("Content-Type"), equalTo(MediaType.APPLICATION_OCTET_STREAM));
        assertThat(response.getHeaderString("Content-Disposition"), startsWith("attachment; filename="));
        assertThat(response.getHeaderString("Content-Disposition"), endsWith(".zip"));

        InputStream is = response.readEntity(InputStream.class);
        Set<String> zipEntryNames = getEntries(is).keySet();

        assertThat(zipEntryNames, hasItem("register.json"));
        assertThat(zipEntryNames.stream().filter(e -> e.matches("(entry/)(\\d)(.json)")).count(), is(5L));
        assertThat(zipEntryNames.stream().filter(e -> e.matches("(item/)(\\w+)(.json)")).count(), is(4L));
    }

    @Test
    public void downloadRegister_shouldUseCorrectEntryAndItemJsonFormat() throws IOException {
        Response response = getRequest("/download-register");
        InputStream is = response.readEntity(InputStream.class);

        List<String> itemJson = getEntries(is).entrySet().stream()
                .filter(j -> j.getKey().startsWith("item"))
                .map(j -> j.getValue().toString())
                .collect(Collectors.toList());

        assertThat(itemJson, hasItems(item1, item2, item3, item4));

        List<JsonNode> entryJson = getEntries(is).entrySet().stream()
                .filter(j -> j.getKey().startsWith("entry"))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        entryJson.forEach(j -> {
            assertTrue(j.has("entry-number"));
            assertTrue(j.has("entry-timestamp"));
            assertTrue(j.has("item-hash"));
        });
    }

    @Test
    public void downloadRegister_shouldUseCorrectRegisterJsonFormat() throws IOException {
        Response response = getRequest("/download-register");
        InputStream is = response.readEntity(InputStream.class);

        JsonNode registerJson = getEntries(is).get("register.json");

        assertThat(registerJson.get("total-entries").asInt(), is(5));
        assertThat(registerJson.get("total-records").asInt(), is(3));
        assertTrue(registerJson.has("last-updated"));
        assertTrue(registerJson.has("domain"));
        assertTrue(registerJson.has("register-record"));
    }

    @Test
    public void downloadRSF_shouldReturnRegisterAsRsfStream() throws IOException {
        Response response = getRequest("/download-rsf");

        assertThat(response.getHeaderString("Content-Type"), equalTo(ExtraMediaType.APPLICATION_RSF));
        assertThat(response.getHeaderString("Content-Disposition"), startsWith("attachment; filename="));
        assertThat(response.getHeaderString("Content-Disposition"), endsWith(".tsv"));

        String[] rsfLines = getRsfLinesFrom(response);

        assertThat(rsfLines[0], equalTo("assert-root-hash\te3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"));

        assertThat(rsfLines[1], equalTo("add-item\t{\"address\":\"12345\",\"street\":\"ellis\"}"));
        assertThat(rsfLines[2], equalTo("add-item\t{\"address\":\"145678\",\"street\":\"ellis\"}"));
        assertThat(rsfLines[3], equalTo("add-item\t{\"address\":\"6789\",\"street\":\"presley\"}"));
        assertThat(rsfLines[4], equalTo("add-item\t{\"address\":\"12345\",\"street\":\"foo\"}"));

        assertFormattedEntry(rsfLines[5], "sha-256:19205fafe65406b9b27fce1b689abc776df4ddcf150c28b29b73b4ea054af6b9\t12345");
        assertFormattedEntry(rsfLines[6], "sha-256:bd239db51960376826b937a615f0f3397485f00611d35bb7e951e357bf73b934\t6789");
        assertFormattedEntry(rsfLines[7], "sha-256:cc8a7c42275c84b94c6e282ae88b3dbcc06319156fc4539a2f39af053bf30592\t12345");
        assertFormattedEntry(rsfLines[8], "sha-256:8ac926428ee49fb83c02bdd2556e62e84cfd9e636cd35eb1306ac8cb661e4983\t145678");
        assertFormattedEntry(rsfLines[9], "sha-256:19205fafe65406b9b27fce1b689abc776df4ddcf150c28b29b73b4ea054af6b9\t12345");

        assertThat(rsfLines[10], containsString("assert-root-hash\t"));
    }

    @Test
    public void downloadPartialRSF_shouldReturnAPartOfRegisterAsRsfStream() throws IOException {
        Response response = getRequest("/download-rsf/1/2");

        assertThat(response.getHeaderString("Content-Type"), equalTo(ExtraMediaType.APPLICATION_RSF));
        assertThat(response.getHeaderString("Content-Disposition"), startsWith("attachment; filename="));
        assertThat(response.getHeaderString("Content-Disposition"), endsWith(".tsv"));

        String[] rsfLines = getRsfLinesFrom(response);

        assertThat(rsfLines[0], equalTo("assert-root-hash\te3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"));

        assertThat(rsfLines[1], equalTo("add-item\t{\"address\":\"12345\",\"street\":\"ellis\"}"));
        assertThat(rsfLines[2], equalTo("add-item\t{\"address\":\"6789\",\"street\":\"presley\"}"));

        assertFormattedEntry(rsfLines[3], "sha-256:19205fafe65406b9b27fce1b689abc776df4ddcf150c28b29b73b4ea054af6b9\t12345");
        assertFormattedEntry(rsfLines[4], "sha-256:bd239db51960376826b937a615f0f3397485f00611d35bb7e951e357bf73b934\t6789");

        assertThat(rsfLines[5], containsString("assert-root-hash\t"));
    }

    @Test
    public void downloadPartialRSF_shouldReturnReturn404ForRsfBoundariesInTheIncorrectOrder() throws IOException {
        Response response = getRequest("/download-rsf/4/1");
        
        assertThat(response.getStatus(), equalTo(400));
    }

    @Test
    public void downloadPartialRSF_shouldReturnReturn404ForRsfBoundariesOutOfBound() throws IOException {
        Response response = getRequest("/download-rsf/666/1000");

        assertThat(response.getStatus(), equalTo(400));
    }

    @Test
    public void downloadPartialRSF_shouldReturnSameRSFAsFullDownload() {
        Response fullRsfResponse = getRequest("/download-rsf");
        Response partialRsfResponse = getRequest("/download-rsf/1/5");

        String[] fullRsfLines = getRsfLinesFrom(fullRsfResponse);
        String[] partialRsfLines = getRsfLinesFrom(partialRsfResponse);

        assertThat(fullRsfLines, equalTo(partialRsfLines));
    }

    private String[] getRsfLinesFrom(Response response){
        InputStream is = response.readEntity(InputStream.class);
        return new BufferedReader(new InputStreamReader(is)).lines().toArray(String[]::new);
    }

    private void assertFormattedEntry(String actualEntry, String expectedLine){
        assertThat(actualEntry, startsWith("append-entry"));
        assertThat(actualEntry, endsWith(expectedLine));
    }

    private Map<String, JsonNode> getEntries(InputStream inputStream) throws IOException {
        ZipInputStream zis = new ZipInputStream(inputStream);

        Map<String, JsonNode> entries = new HashMap<>();
        byte[] buffer = new byte[1024];
        int read = 0;
        for (ZipEntry entry; (entry = zis.getNextEntry()) != null; ) {
            StringBuilder sb = new StringBuilder();
            while ((read = zis.read(buffer, 0, 1024)) >= 0) {
                sb.append(new String(buffer, 0, read));
            }

            entries.put(entry.getName(), new ObjectMapper().readTree(sb.toString()));
        }

        return entries;
    }
}
