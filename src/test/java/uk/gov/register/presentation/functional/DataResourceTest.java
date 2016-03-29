package uk.gov.register.presentation.functional;

import com.google.common.collect.ImmutableList;
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

public class DataResourceTest extends FunctionalTestBase {
    @Before
    public void publishTestMessages() {
        dbSupport.publishMessages(ImmutableList.of(
                "{\"hash\":\"hash1\",\"entry\":{\"name\":\"ellis\",\"address\":\"12345\"}}",
                "{\"hash\":\"hash2\",\"entry\":{\"name\":\"presley\",\"address\":\"6789\"}}",
                "{\"hash\":\"hash3\",\"entry\":{\"name\":\"ellis\",\"address\":\"145678\"}}"
        ));
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
