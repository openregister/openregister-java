package uk.gov.register.presentation;

import uk.gov.register.proofs.ct.SignedTreeHead;

public class Proofs {
    private SignedTreeHead certificateTransparencySignedTreeHead;

    public Proofs(SignedTreeHead certificateTransparencySignedTreeHead) {
        this.certificateTransparencySignedTreeHead = certificateTransparencySignedTreeHead;
    }

    public SignedTreeHead getCertificateTransparencySignedTreeHead() {
        return certificateTransparencySignedTreeHead;
    }
}
