package uk.gov.register.presentation;

import uk.gov.register.proofs.ct.SignedTreeHead;

public class Proofs {
    private SignedTreeHead certificateTransparencySignedTreeHead;

    public SignedTreeHead getCertificateTransparencySignedTreeHead() {
        return certificateTransparencySignedTreeHead;
    }

    public void setCertificateTransparencySignedTreeHead(SignedTreeHead certificateTransparencySignedTreeHead) {
        this.certificateTransparencySignedTreeHead = certificateTransparencySignedTreeHead;
    }
}
