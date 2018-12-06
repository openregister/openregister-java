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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static uk.gov.register.functional.app.RsfRegisterDefinition.ADDRESS_FIELDS;
import static uk.gov.register.functional.app.RsfRegisterDefinition.ADDRESS_REGISTER;
import static uk.gov.register.functional.app.RsfRegisterDefinition.POSTCODE_REGISTER;
import static uk.gov.register.functional.app.TestRegister.address;
import static uk.gov.register.functional.app.TestRegister.postcode;

public class VerifiableLogResourceFunctionalTest {

    @ClassRule
    public static RegisterRule register = new RegisterRule();
    private final String proofIdentifier = "merkle:sha-256";

    @Before
    public void publishTestMessages() throws Throwable {
        register.wipe();
        register.loadRsfV1(address, ADDRESS_FIELDS + ADDRESS_REGISTER +
            "add-item\t{\"address\":\"1111\",\"street\":\"elvis\"}\n" +
            "append-entry\tuser\t1111\t2017-06-12T14:05:01Z\tsha-256:c4ede5d49a5d0ea7babbc097b0905acf2bb5146f288c8b33ba3e29762196566f\n" +
            "add-item\t{\"address\":\"2222\",\"street\":\"presley\"}\n" +
            "append-entry\tuser\t2222\t2017-06-12T14:05:01Z\tsha-256:c151ac3871e0b2cf5d8c5b1251188ebddf15f9c7f7718f2d925e17e449ea8095\n" +
            "add-item\t{\"address\":\"3333\",\"street\":\"ellis\"}\n" +
            "append-entry\tuser\t3333\t2017-06-12T14:05:01Z\tsha-256:cf79274252a12009e2b39bd853b1e113c202e3b29fdb6cb9d8a705643c8d90bb\n" +
            "add-item\t{\"address\":\"4444\",\"street\":\"pretzel\"}\n" +
            "append-entry\tuser\t4444\t2017-06-12T14:05:01Z\tsha-256:2fd84ec46a59a0e1f624abc6fb2c4ee4159bdf4e9ffed64252b4fcddefb62ea3\n" +
            "add-item\t{\"address\":\"5555\",\"street\":\"elfsley\"}\n" +
            "append-entry\tuser\t5555\t2017-06-12T14:05:01Z\tsha-256:4882ebcc80b9005597f571019efc23e8a36422623cd4811c2213f44d4faf9545");

        register.loadRsfV1(postcode, POSTCODE_REGISTER +
            "add-item\t{\"postcode\":\"P1\"}\n" +
            "append-entry\tuser\tP1\t2017-06-12T14:06:30Z\tsha-256:50a5de96d4cb6341a2f18c0b34bc401b2c92e3ac46641c0d1014dc82ed498326\n" +
            "add-item\t{\"postcode\":\"P2\"}\n" +
            "append-entry\tuser\tP2\t2017-06-12T14:06:30Z\tsha-256:af0cf1489bfb0f3da8130a50e6b4fa49de7c480a416dfa710418c0e42dc72949\n" +
            "add-item\t{\"postcode\":\"P3\"}\n" +
            "append-entry\tuser\tP3\t2017-06-12T14:06:30Z\tsha-256:e345a3259a175b1484e09640f1ba789dc4af410a01b828083280a2c4ecd0328e\n" +
            "add-item\t{\"postcode\":\"P4\"}\n" +
            "append-entry\tuser\tP4\t2017-06-12T14:06:30Z\tsha-256:3c6435bd0c1241c0be7489e34187c818104db64540140d2510da09cde6826724\n" +
            "add-item\t{\"postcode\":\"P5\"}\n" +
            "append-entry\tuser\tP5\t2017-06-12T14:06:30Z\tsha-256:0e5e2a727e72128c569e6f725a6a6ff900350fd85bf9044ae8a6a57dbbb6d800");
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
        Response response = register.getRequest(address, "/proof/entries/1/5/" + proofIdentifier);

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

        Response entry1Proof = register.getRequest(address, "/proof/entries/1/2/" + proofIdentifier);
        Response entry2Proof = register.getRequest(address, "/proof/entries/2/2/" + proofIdentifier);

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

        List<String> addressAuditPath = (List<String>) register.getRequest(address, "/proof/entries/3/5/" + proofIdentifier)
                .readEntity(Map.class).get("merkle-audit-path");
        List<String> postcodeAuditPath = (List<String>) register.getRequest(postcode, "/proof/entries/3/5/" + proofIdentifier)
                .readEntity(Map.class).get("merkle-audit-path");

        assertThat(addressAuditPath, everyItem(not(isIn(postcodeAuditPath))));
    }


    @Test
    public void registerProofDiffersFromV1() {
        String v1Proof = register.getRequest(address, "/proof/register/merkle:sha-256").readEntity(Map.class).get("root-hash").toString();
        String nextProof = register.getRequest(address, "/next/proof/register").readEntity(Map.class).get("root-hash").toString();

        assertTrue(nextProof.length() > 0);
        assertNotEquals(v1Proof, nextProof);
    }

    @Test
    public void entryProofDiffersFromV1() {
        Map v1Proof = register.getRequest(address, "/proof/entries/1/2/merkle:sha-256").readEntity(Map.class);
        Map nextProof = register.getRequest(address, "/next/proof/entry/1/2").readEntity(Map.class);
        List v1AuditPath = (List)(v1Proof.get("merkle-audit-path"));
        List nextAuditPath = (List)(nextProof.get("merkle-audit-path"));

        assertFalse(nextAuditPath.isEmpty());
        assertNotEquals(v1AuditPath, nextAuditPath);
    }

    @Test
    public void consistencyProofDiffersFromV1() {
        Map v1Proof = register.getRequest(address, "/proof/consistency/2/5/merkle:sha-256").readEntity(Map.class);
        Map nextProof = register.getRequest(address, "/next/proof/consistency/2/5").readEntity(Map.class);
        List v1ConsistencyNodes = (List)(v1Proof.get("merkle-consistency-nodes"));
        List nextConsistencyNodes = (List)(nextProof.get("merkle-consistency-nodes"));

        assertFalse(nextConsistencyNodes.isEmpty());
        assertNotEquals(v1ConsistencyNodes, nextConsistencyNodes);
    }
}
