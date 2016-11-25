package uk.gov.register.functional;

import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isEmptyOrNullString;

public class VerifiableLogResourceFunctionalTest extends FunctionalTestBase {

    private final String proofIdentifier = "merkle:sha-256";

    @Before
    public void publishTestMessages() throws Throwable {
        mintItems("{\"address\":\"1111\",\"street\":\"elvis\"}",
                "{\"address\":\"2222\",\"street\":\"presley\"}",
                "{\"address\":\"3333\",\"street\":\"ellis\"}",
                "{\"address\":\"4444\",\"street\":\"pretzel\"}",
                "{\"address\":\"5555\",\"street\":\"elfsley\"}");
    }

    @Test
    public void getRegisterProof() {
        Response response = register.getRequest("/proof/register/" + proofIdentifier);

        assertThat(response.getStatus(), equalTo(200));

        Map<?,?> responseData = response.readEntity(Map.class);
        assertThat(responseData.get("proof-identifier").toString(), equalTo(proofIdentifier));
        assertThat(responseData.get("root-hash").toString(), not(isEmptyOrNullString()));
    }

    @Test
    public void getEntryProof() {
        Response response = register.getRequest("/proof/entry/1/5/" + proofIdentifier);

        assertThat(response.getStatus(), equalTo(200));

        Map<?,?> responseData = response.readEntity(Map.class);
        assertThat(responseData.get("proof-identifier").toString(), equalTo(proofIdentifier));
        assertThat(responseData.get("entry-number").toString(), equalTo("1"));

        List<?> auditPath = (List)(responseData.get("merkle-audit-path"));
        assertThat(auditPath, hasSize(3));
    }

    @Test
    public void getConsistencyProof() {
        Response response = register.getRequest("/proof/consistency/2/5/" + proofIdentifier);

        assertThat(response.getStatus(), equalTo(200));

        Map<?,?> responseData = response.readEntity(Map.class);
        assertThat(responseData.get("proof-identifier").toString(), equalTo(proofIdentifier));

        List<?> auditPath = (List)(responseData.get("merkle-consistency-nodes"));
        assertThat(auditPath, hasSize(2));
    }
}
