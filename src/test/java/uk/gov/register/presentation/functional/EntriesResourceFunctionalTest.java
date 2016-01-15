package uk.gov.register.presentation.functional;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.junit.Test;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class EntriesResourceFunctionalTest extends FunctionalTestBase {
    @Test
    public void entries_returnsEmptyResultJsonWhenNoEntryIsAvailable() {
        Response response = getRequest("/entries.json");
        assertThat(response.getStatus(), equalTo(200));
        assertThat(response.readEntity(ArrayNode.class).size(), equalTo(0));
    }

    @Test
    public void entries_setsAppropriateFilenameForDownload() {
        Response response = getRequest("address", "/entries.json");
        assertThat(response.getStatus(), equalTo(200));
        assertThat(response.getHeaderString(HttpHeaders.CONTENT_DISPOSITION), containsString("filename=\"address-entries.json\""));
    }
}
