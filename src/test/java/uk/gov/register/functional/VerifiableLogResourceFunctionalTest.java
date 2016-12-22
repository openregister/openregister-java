package uk.gov.register.functional;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.register.functional.app.RegisterRule;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.isIn;
import static uk.gov.register.functional.app.TestRegister.address;
import static uk.gov.register.functional.app.TestRegister.postcode;

public class VerifiableLogResourceFunctionalTest {

    @ClassRule
    public static RegisterRule register = new RegisterRule();
    private final String proofIdentifier = "merkle:sha-256";

    @Before
    public void publishTestMessages() throws Throwable {
        register.wipe();
        register.mintLines(address, "{\"address\":\"1111\",\"street\":\"elvis\"}", "{\"address\":\"2222\",\"street\":\"presley\"}", "{\"address\":\"3333\",\"street\":\"ellis\"}", "{\"address\":\"4444\",\"street\":\"pretzel\"}", "{\"address\":\"5555\",\"street\":\"elfsley\"}");
        register.mintLines(postcode, "{\"postcode\":\"P1\"}", "{\"postcode\":\"P2\"}", "{\"postcode\":\"P3\"}", "{\"postcode\":\"P4\"}", "{\"postcode\":\"P5\"}");
    }

    @Test
    public void getRegisterProof() {
        Response response = register.getRequest(address, "/proof/register/" + proofIdentifier);

        assertThat(response.getStatus(), equalTo(200));

        Map<?,?> responseData = response.readEntity(Map.class);
        assertThat(responseData.get("proof-identifier").toString(), equalTo(proofIdentifier));
        assertThat(responseData.get("root-hash").toString(), not(isEmptyOrNullString()));
    }

    @Test
    public void getEntryProof() {
        Response response = register.getRequest(address, "/proof/entry/1/5/" + proofIdentifier);

        assertThat(response.getStatus(), equalTo(200));

        Map<?,?> responseData = response.readEntity(Map.class);
        assertThat(responseData.get("proof-identifier").toString(), equalTo(proofIdentifier));
        assertThat(responseData.get("entry-number").toString(), equalTo("1"));

        List<?> auditPath = (List)(responseData.get("merkle-audit-path"));
        assertThat(auditPath, hasSize(3));
    }

    @Test
    public void getConsistencyProof() {
        Response response = register.getRequest(address, "/proof/consistency/2/5/" + proofIdentifier);

        assertThat(response.getStatus(), equalTo(200));

        Map<?,?> responseData = response.readEntity(Map.class);
        assertThat(responseData.get("proof-identifier").toString(), equalTo(proofIdentifier));

        List<?> auditPath = (List)(responseData.get("merkle-consistency-nodes"));
        assertThat(auditPath, hasSize(2));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void registerProofsForDifferentRegistersAreIndependent() {
        // This test exists to ensure that we aren't accidentally sharing the same MemoizationStore between
        // two different register instances.
        // We get a proof of the same shape from two different registers, and verify that there are no
        // hashes that exist in both proofs.

        List<String> addressAuditPath = (List<String>) register.getRequest(address, "/proof/entry/3/5/" + proofIdentifier)
                .readEntity(Map.class).get("merkle-audit-path");
        List<String> postcodeAuditPath = (List<String>) register.getRequest(postcode, "/proof/entry/3/5/" + proofIdentifier)
                .readEntity(Map.class).get("merkle-audit-path");

        assertThat(addressAuditPath, everyItem(not(isIn(postcodeAuditPath))));
    }
}
