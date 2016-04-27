package uk.gov.register.presentation.functional;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.register.presentation.functional.TestEntry.anEntry;

public class DataResourceTest extends FunctionalTestBase {
    @Before
    public void publishTestMessages() {
        dbSupport.publishEntries(
                Lists.newArrayList(
                        anEntry(1, "{\"name\":\"ellis\",\"address\":\"12345\"}"),
                        anEntry(2, "{\"name\":\"presley\",\"address\":\"6789\"}"),
                        anEntry(3, "{\"name\":\"foo\",\"address\":\"12345\"}"),
                        anEntry(4, "{\"name\":\"ellis\",\"address\":\"145678\"}"),
                        anEntry(5, "{\"name\":\"ellis\",\"address\":\"12345\"}")
                )
        );
    }

    @Test
    public void downloadRegister_shouldReturnAZipfile() throws IOException {
        Response response = getRequest("/download-register");

        assertThat(response.getHeaderString("Content-Type"), equalTo(MediaType.APPLICATION_OCTET_STREAM));
        assertThat(response.getHeaderString("Content-Disposition"), startsWith("attachment; filename="));
        assertThat(response.getHeaderString("Content-Disposition"), endsWith(".zip"));
        InputStream is = response.readEntity(InputStream.class);
        assertThat(getEntries(is), hasItem("register.json"));
    }

    private List<String> getEntries(InputStream inputStream) throws IOException {
        ZipInputStream zis = new ZipInputStream(inputStream);

        List<String> entries = new ArrayList<>();
        for (ZipEntry entry; (entry = zis.getNextEntry()) != null; ) {
            entries.add(entry.getName());
        }

        return entries;
    }
}
