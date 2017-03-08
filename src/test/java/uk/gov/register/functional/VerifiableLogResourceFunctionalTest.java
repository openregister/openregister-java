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
import static org.hamcrest.Matchers.*;
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
        assertThat(responseData.get("total-entries"), is(5));
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

    @Test
    public void entryProofsAreDifferentForEntriesInSameSubtree() {
        // This tests that we are mapping entry numbers (one-indexed) to leaf indexes correctly (zero-indexed).
        // In the case that we accidentally map to zero-indexed entry nubers this test would fail

        Response entry1Proof = register.getRequest(address, "/proof/entry/1/2/" + proofIdentifier);
        Response entry2Proof = register.getRequest(address, "/proof/entry/2/2/" + proofIdentifier);

        assertThat(entry1Proof.getStatus(), equalTo(200));
        assertThat(entry2Proof.getStatus(), equalTo(200));

        Map<?,?> entry1ProofData = entry1Proof.readEntity(Map.class);
        Map<?,?> entry2ProofData = entry2Proof.readEntity(Map.class);

        List<?> entry1Path = (List)(entry1ProofData.get("merkle-audit-path"));
        List<?> entry2Path = (List)(entry2ProofData.get("merkle-audit-path"));

        assertThat(entry1Path, hasSize(1));
        assertThat(entry2Path, hasSize(1));
        assertThat(entry1Path, not(equalTo(entry2Path)));
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
