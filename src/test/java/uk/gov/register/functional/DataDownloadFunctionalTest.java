package uk.gov.register.functional;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import uk.gov.register.util.CanonicalJsonMapper;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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

    private final String item1 = "{\"address\":\"12345\",\"street\":\"ellis\"}";
    private final String item2 = "{\"address\":\"6789\",\"street\":\"presley\"}";
    private final String item3 =  "{\"address\":\"12345\",\"street\":\"foo\"}";
    private final String item4 =  "{\"address\":\"145678\",\"street\":\"ellis\"}";

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
        CanonicalJsonMapper canonicalJsonMapper = new CanonicalJsonMapper();

        // Hacking this test to get it to canonicalize on-the-fly, as data is being retrieved from Beta register
        List<String> itemJson = getEntries(is).entrySet().stream()
                .filter(j -> j.getKey().startsWith("item"))
                .map(j -> j.getValue().toString())
                .map(j -> new String(canonicalJsonMapper.writeToBytes(canonicalJsonMapper.readFromBytes(j.getBytes(StandardCharsets.UTF_8))), StandardCharsets.UTF_8))
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

            entries.put(entry.getName(), new CanonicalJsonMapper().readFromBytes(sb.toString().getBytes()));
        }

        return entries;
    }
}
