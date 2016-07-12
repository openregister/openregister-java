package uk.gov.register.presentation.functional;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import io.dropwizard.jackson.Jackson;
import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.time.Instant;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.register.presentation.functional.TestEntry.anEntry;

@Ignore("this test depends on the time that the entries are minted")
public class VerifiableLogResourceFunctionalTest extends FunctionalTestBase {

    private static final String item1 = "{\"address\":\"6789\",\"name\":\"presley\"}";
    private static final String item2 = "{\"address\":\"6789\",\"name\":\"presley2\"}";
    private static final String item3 = "{\"address\":\"6790\",\"name\":\"rose cottage\"}";

    private static final TestEntry entry1 = anEntry(1, item1, Instant.parse("2016-07-01T11:21:30.00Z"));
    private static final TestEntry entry2 = anEntry(2, item2, Instant.parse("2016-07-01T11:21:35.00Z"));
    private static final TestEntry entry3 = anEntry(3, item3, Instant.parse("2016-07-01T11:22:10.00Z"));

    @Test
    public void shouldReturnRegisterProof() throws IOException {
        addEntryAndAssertRootHash(entry1, "d3d33f57b033d18ad11e14b28ef6f33487410c98548d1759c772370dfeb6db11");
        addEntryAndAssertRootHash(entry2, "e869291e3017a7b1dd6b16af0b556d75378bef59486f1a7f53586b3ca86aed09");
        addEntryAndAssertRootHash(entry3, "6b85b168f7c5f0587fc22ff4ba6937e61b33f6e89b70eed53d78d895d35dc9c3");
    }

    private void addEntryAndAssertRootHash(TestEntry entry, String expectedRootHash) throws IOException {
        dbSupport.publishEntries(ImmutableList.of(entry));
        Response response = getRequest("/proof/register/merkle:sha-256");
        assertThat(response.getStatus(), equalTo(200));
        JsonNode jsonResult = Jackson.newObjectMapper().readValue(response.readEntity(String.class), JsonNode.class);
        assertThat(jsonResult.get("proof-identifier").textValue(), equalTo("merkle:sha-256"));
        assertThat(jsonResult.get("root-hash").textValue(), equalTo(expectedRootHash));
    }
}
