package uk.gov.register.presentation.functional;

import com.google.common.collect.ImmutableList;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.gov.register.presentation.functional.testSupport.DBSupport;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

public class DataResourceTest extends FunctionalTestBase {
    @BeforeClass
    public static void publishTestMessages() {
        DBSupport.publishMessages(ImmutableList.of(
                "{\"hash\":\"hash1\",\"entry\":{\"name\":\"ellis\",\"address\":\"12345\"}}",
                "{\"hash\":\"hash2\",\"entry\":{\"name\":\"presley\",\"address\":\"6789\"}}",
                "{\"hash\":\"hash3\",\"entry\":{\"name\":\"ellis\",\"address\":\"145678\"}}"
        ));
    }

    @Test
    public void download_shouldReturnAZipfile() {
        Response response = getRequest("/download");
        assertThat(response.getHeaderString("Content-Type"), equalTo(MediaType.APPLICATION_OCTET_STREAM));
        assertThat(response.getHeaderString("Content-Disposition"), startsWith("attachment; filename="));
        assertThat(response.getHeaderString("Content-Disposition"), endsWith(".zip"));
        InputStream is = response.readEntity(InputStream.class);
        ZipInputStream zis = new ZipInputStream(is);
        ZipEntry entry = null;
        try {
            while ((entry = zis.getNextEntry()) != null) {
                System.out.println(entry.getName());
            }
        } catch (IOException e) {
            fail("Unexpected IO exception - " + e.getMessage());
        }
    }
}