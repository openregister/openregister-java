package uk.gov.register.functional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import uk.gov.register.core.EntryType;
import uk.gov.register.functional.app.RegisterRule;
import uk.gov.register.functional.app.RsfRegisterDefinition;
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
import static uk.gov.register.functional.app.TestRegister.address;

public class DataDownloadFunctionalTest {

    @ClassRule
    public static RegisterRule register = new RegisterRule();
    private final String item1 = "{\"street\":\"ellis\",\"address\":\"12345\"}";
    private final String item2 = "{\"street\":\"presley\",\"address\":\"6789\"}";
    private final String item3 = "{\"street\":\"foo\",\"address\":\"12345\"}";
    private final String item4 = "{\"street\":\"ellis\",\"address\":\"145678\"}";

    @Before
    public void publishTestMessages() {
        register.wipe();
        register.loadRsf(address, RsfRegisterDefinition.ADDRESS_NAME + RsfRegisterDefinition.ADDRESS_FIELDS + RsfRegisterDefinition.ADDRESS_REGISTER);
        register.loadRsf(
            address,
            "add-item\t{\"custodian\":\"John Smith\"}\n" +
            "add-item\t{\"address\":\"12345\",\"street\":\"ellis\"}\n" +
            "add-item\t{\"address\":\"6789\",\"street\":\"presley\"}\n" +
            "add-item\t{\"address\":\"12345\",\"street\":\"foo\"}\n" +
            "add-item\t{\"address\":\"145678\",\"street\":\"ellis\"}\n" +
            "append-entry\tsystem\tcustodian\t2017-06-01T10:00:00Z\tsha-256:7652aabbc817e434b1b6aedffe58582412c79be9d2ebcb12071d3f7fe7fe96d8\n" +
            "append-entry\tuser\t12345\t2017-06-01T10:13:27Z\tsha-256:19205fafe65406b9b27fce1b689abc776df4ddcf150c28b29b73b4ea054af6b9\n" +
            "append-entry\tuser\t6789\t2017-06-01T10:13:27Z\tsha-256:bd239db51960376826b937a615f0f3397485f00611d35bb7e951e357bf73b934\n" +
            "append-entry\tuser\t12345\t2017-06-01T10:13:27Z\tsha-256:cc8a7c42275c84b94c6e282ae88b3dbcc06319156fc4539a2f39af053bf30592\n" +
            "append-entry\tuser\t145678\t2017-06-01T10:13:27Z\tsha-256:8ac926428ee49fb83c02bdd2556e62e84cfd9e636cd35eb1306ac8cb661e4983\n" +
            "append-entry\tuser\t12345\t2017-06-01T10:13:27Z\tsha-256:19205fafe65406b9b27fce1b689abc776df4ddcf150c28b29b73b4ea054af6b9");
    }

    @Test
    public void downloadRegister_shouldReturnAZipfile() throws IOException {
        Response response = register.getRequest(address, "/download-register");

        assertThat(response.getHeaderString("Content-Type"), equalTo(MediaType.APPLICATION_OCTET_STREAM));
        assertThat(response.getHeaderString("Content-Disposition"), startsWith("attachment; filename="));
        assertThat(response.getHeaderString("Content-Disposition"), endsWith(".zip"));

        InputStream is = response.readEntity(InputStream.class);
        Set<String> zipEntryNames = getEntries(is).keySet();

        // TODO: Revisit this later to determine if item count should be 17 (metadata included) or 4 (without metadata)
        assertThat(zipEntryNames, hasItem("register.json"));
        assertThat(zipEntryNames.stream().filter(e -> e.matches("(entry/)(\\d+)(.json)")).count(), is(5L));
        assertThat(zipEntryNames.stream().filter(e -> e.matches("(item/)(\\w+)(.json)")).count(), is(17L));
    }

    @Test
    public void downloadRegister_shouldUseCorrectEntryAndItemJsonFormat() throws IOException {
        Response response = register.getRequest(address, "/download-register");
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
        Response response = register.getRequest(address, "/download-register");
        InputStream is = response.readEntity(InputStream.class);

        JsonNode registerJson = getEntries(is).get("register.json");

        assertThat(registerJson.get("total-entries").asInt(), is(5));
        assertThat(registerJson.get("total-records").asInt(), is(3));
        assertTrue(registerJson.has("last-updated"));
        assertTrue(registerJson.has("domain"));
        assertTrue(registerJson.has("register-record"));
    }

    @Test
    public void downloadRSF_shouldReturnCustodianNameAsTwentySeventhEntry() {
        Response response = register.getRequest(address, "/download-rsf/0/1");

        List<String> rsfLines = getRsfLinesFrom(response);

        assertThat(rsfLines.get(0), equalTo("assert-root-hash\tsha-256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"));
        assertThat(rsfLines.contains("add-item\t{\"custodian\":\"John Smith\"}"), is(true));
        assertThat(rsfLines.get(27), equalTo("append-entry\tsystem\tcustodian\t2017-06-01T10:00:00Z\tsha-256:7652aabbc817e434b1b6aedffe58582412c79be9d2ebcb12071d3f7fe7fe96d8"));
    }

    @Test
    public void downloadRSF_shouldReturnRegisterNameAsFirstEntry() {
        Response response = register.getRequest(address, "/download-rsf/0/1");

        List<String> rsfLines = getRsfLinesFrom(response);

        assertThat(rsfLines.get(0), equalTo("assert-root-hash\tsha-256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"));
        assertThat(rsfLines.contains("add-item\t{\"name\":\"address\"}"), is(true));
        assertThat(rsfLines.get(15), equalTo("append-entry\tsystem\tname\t2017-06-01T10:00:00Z\tsha-256:eb5064e317d2d673634e4a40782ab727573bd2075a82cf69c05af919d7518794"));
    }

    @Test
    public void downloadRSF_shouldReturnRegisterAsRsfStream() throws IOException {
        Response response = register.getRequest(address, "/download-rsf");

        assertThat(response.getHeaderString("Content-Type"), equalTo(ExtraMediaType.APPLICATION_RSF));
        assertThat(response.getHeaderString("Content-Disposition"), startsWith("attachment; filename="));
        assertThat(response.getHeaderString("Content-Disposition"), endsWith(".rsf"));

        List<String> rsfLines = getRsfLinesFrom(response);

        assertThat(rsfLines.get(0), equalTo("assert-root-hash\tsha-256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"));

        assertThat(rsfLines, hasItems(
                "add-item\t{\"address\":\"12345\",\"street\":\"ellis\"}",
                "add-item\t{\"address\":\"145678\",\"street\":\"ellis\"}",
                "add-item\t{\"address\":\"6789\",\"street\":\"presley\"}",
                "add-item\t{\"address\":\"12345\",\"street\":\"foo\"}"));

        assertFormattedEntry(rsfLines.get(31), EntryType.user, "12345","sha-256:19205fafe65406b9b27fce1b689abc776df4ddcf150c28b29b73b4ea054af6b9");
        assertFormattedEntry(rsfLines.get(32), EntryType.user, "6789","sha-256:bd239db51960376826b937a615f0f3397485f00611d35bb7e951e357bf73b934");
        assertFormattedEntry(rsfLines.get(33), EntryType.user, "12345","sha-256:cc8a7c42275c84b94c6e282ae88b3dbcc06319156fc4539a2f39af053bf30592");
        assertFormattedEntry(rsfLines.get(34), EntryType.user, "145678","sha-256:8ac926428ee49fb83c02bdd2556e62e84cfd9e636cd35eb1306ac8cb661e4983");
        assertFormattedEntry(rsfLines.get(35), EntryType.user, "12345","sha-256:19205fafe65406b9b27fce1b689abc776df4ddcf150c28b29b73b4ea054af6b9");

        assertThat(rsfLines.get(36), containsString("assert-root-hash\t"));
    }

    @Ignore("Ignored as behaves inconsistently locally versus CI")
    @Test
    public void downloadPartialRSF_fromStartEntryNumber_shouldReturnAPartOfRegisterAsRsfStream() {
        Response response = register.getRequest(address, "/download-rsf/2");

        assertThat(response.getHeaderString("Content-Type"), equalTo(ExtraMediaType.APPLICATION_RSF));
        assertThat(response.getHeaderString("Content-Disposition"), startsWith("attachment; filename="));
        assertThat(response.getHeaderString("Content-Disposition"), endsWith(".rsf"));

        List<String> rsfLines = getRsfLinesFrom(response);
        assertThat(rsfLines.get(0), equalTo("assert-root-hash\tsha-256:7cdec03e9aba7810340596079a18b495dbc93f22a8d051d3417fb4a79b1f6124"));

        assertThat(rsfLines, hasItems(
                "add-item\t{\"address\":\"12345\",\"street\":\"foo\"}",
                "add-item\t{\"address\":\"12345\",\"street\":\"ellis\"}",
                "add-item\t{\"address\":\"145678\",\"street\":\"ellis\"}"));

        assertFormattedEntry(rsfLines.get(4), EntryType.user, "12345","sha-256:cc8a7c42275c84b94c6e282ae88b3dbcc06319156fc4539a2f39af053bf30592");
        assertFormattedEntry(rsfLines.get(5), EntryType.user,"145678","sha-256:8ac926428ee49fb83c02bdd2556e62e84cfd9e636cd35eb1306ac8cb661e4983");
        assertFormattedEntry(rsfLines.get(6), EntryType.user,"12345","sha-256:19205fafe65406b9b27fce1b689abc776df4ddcf150c28b29b73b4ea054af6b9");

        assertThat(rsfLines.get(7), containsString("assert-root-hash\t"));
    }

    @Test
    public void downloadPartialRSF_shouldReturnAPartOfRegisterAsRsfStream() throws IOException {
        Response response = register.getRequest(address, "/download-rsf/1/3");

        assertThat(response.getHeaderString("Content-Type"), equalTo(ExtraMediaType.APPLICATION_RSF));
        assertThat(response.getHeaderString("Content-Disposition"), startsWith("attachment; filename="));
        assertThat(response.getHeaderString("Content-Disposition"), endsWith(".rsf"));

        List<String> rsfLines = getRsfLinesFrom(response);

        assertThat(rsfLines.get(0), equalTo("assert-root-hash\tsha-256:1ff5df752df3070a48c52fb6b203f4c2f83a6962484f8d20c911260736d13eb0"));

        assertThat(rsfLines, hasItems(
                "add-item\t{\"address\":\"6789\",\"street\":\"presley\"}",
                "add-item\t{\"address\":\"12345\",\"street\":\"foo\"}"));

        assertFormattedEntry(rsfLines.get(3), EntryType.user, "6789","sha-256:bd239db51960376826b937a615f0f3397485f00611d35bb7e951e357bf73b934");
        assertFormattedEntry(rsfLines.get(4), EntryType.user, "12345","sha-256:cc8a7c42275c84b94c6e282ae88b3dbcc06319156fc4539a2f39af053bf30592");

        assertThat(rsfLines.get(5), containsString("assert-root-hash\t"));
    }

    @Test
    public void downloadPartialRSF_shouldReturn400ForRsfBoundariesOutOfBound() throws IOException {
        Response response = register.getRequest(address, "/download-rsf/666/1000");

        assertThat(response.getStatus(), equalTo(400));
    }

    @Test
    public void downloadPartialRSF_shouldReturn400_whenTotalEntries1OutOfBounds() {
        Response response = register.getRequest(address, "/download-rsf/-1/2");

        assertThat(response.getStatus(), equalTo(400));
    }

    @Test
    public void downloadPartialRSF_shouldReturn400_whenTotalEntries2LessThanTotalEntries1() {
        Response response = register.getRequest(address, "/download-rsf/2/1");

        assertThat(response.getStatus(), equalTo(400));
    }


    @Ignore("Ignored as behaves inconsistently locally versus CI")
    @Test
    public void downloadPartialRSF_shouldReturnCurrentRootHash_whenStartEntryNumbersIsCurrentMaximum() {
        Response response = register.getRequest(address, "/download-rsf/5");
        List<String> partialRsfLines = getRsfLinesFrom(response);
        assertThat(partialRsfLines.get(0), equalTo("assert-root-hash\tsha-256:a0aa6dace50e47c68fd4a79e3d94ba525f827e0a44bb64ed2e5f75cfe051b71c"));
    }

    @Test
    public void downloadPartialRSF_shouldReturnRootHash_whenStartAndEndEntryNumbersEqual() {
        Response response = register.getRequest(address, "/download-rsf/0/0");
        List<String> partialRsfLines = getRsfLinesFrom(response);

        assertThat(partialRsfLines.get(0), equalTo("assert-root-hash\tsha-256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"));
    }

    @Test
    public void downloadPartialRSF_shouldReturn400_whenRequestedTotalEntriesExceedsEntriesInRegister() {
        Response response = register.getRequest(address, "/download-rsf/0/19");

        assertThat(response.getStatus(), equalTo(400));
    }

    @Test
    public void downloadPartialRSF_fromStartEntryNumber_shouldReturn400_whenStartEntryNumberOutOfBounds() {
        Response response = register.getRequest(address, "/download-rsf/-1");

        assertThat(response.getStatus(), equalTo(400));
    }

    @Test
    public void downloadPartialRSF_fromStartEntryNumber_shouldReturn400_whenRequestedStartEntryNumberExceedsEntriesInRegister() {
        Response response = register.getRequest(address, "/download-rsf/19");

        assertThat(response.getStatus(), equalTo(400));
    }

    @Test
    public void downloadPartialRSF_fromStartEntryNumber_shouldReturnSameRSFAsFullDownload() {
        Response fullRsfResponse = register.getRequest(address, "/download-rsf");
        Response partialRsfResponse = register.getRequest(address, "/download-rsf/0");

        List<String> fullRsfLines = getRsfLinesFrom(fullRsfResponse);
        List<String> partialRsfLines = getRsfLinesFrom(partialRsfResponse);

        assertThat(partialRsfLines.get(0), is(fullRsfLines.get(0)));
        assertThat(partialRsfLines.subList(1, 20).containsAll(fullRsfLines.subList(1, 20)), is(true));
        assertThat(partialRsfLines.subList(20, 36), equalTo(fullRsfLines.subList(20, 36)));
        assertThat(partialRsfLines.get(36), is(fullRsfLines.get(36)));
    }

    @Test
    public void downloadPartialRSF_shouldReturnSameRSFAsFullDownload() {
        Response fullRsfResponse = register.getRequest(address, "/download-rsf");
        Response partialRsfResponse = register.getRequest(address, "/download-rsf/0/5");

        List<String> fullRsfLines = getRsfLinesFrom(fullRsfResponse);
        List<String> partialRsfLines = getRsfLinesFrom(partialRsfResponse);

        assertThat(partialRsfLines.get(0), is(fullRsfLines.get(0)));
        assertThat(partialRsfLines.subList(1, 20).containsAll(fullRsfLines.subList(1, 20)), is(true));
        assertThat(partialRsfLines.subList(20, 36), equalTo(fullRsfLines.subList(20, 36)));
        assertThat(partialRsfLines.get(36), is(fullRsfLines.get(36)));
    }

    private List<String> getRsfLinesFrom(Response response) {
        InputStream is = response.readEntity(InputStream.class);
        return new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.toList());
    }

    private void assertFormattedEntry(String actualEntry, EntryType entryType, String expectedKey, String expectedHash) {
        String[] parts = actualEntry.split("\t");
        assertThat(parts.length, is(5));
        assertThat(parts[0], is("append-entry"));
        assertThat(parts[1], is(entryType.toString()));
        assertThat(parts[2], is(expectedKey));
        assertThat(parts[4], is(expectedHash));
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
