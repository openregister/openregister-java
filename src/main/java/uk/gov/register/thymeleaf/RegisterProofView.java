package uk.gov.register.thymeleaf;

import com.fasterxml.jackson.annotation.JsonValue;
import io.dropwizard.views.View;
import uk.gov.register.proofs.ct.SignedTreeHead;


public class RegisterProofView extends View {
    private final SignedTreeHead signedTreeHead;

    public RegisterProofView(SignedTreeHead signedTreeHead) {
        super("");
        this.signedTreeHead = signedTreeHead;
    }

    @SuppressWarnings("unused, represents the json response")
    @JsonValue
    public SignedTreeHead getSignedTreeHead() {
        return signedTreeHead;
    }
}
