package uk.gov.register.util;

import uk.gov.register.core.HashingAlgorithm;

public class HashValue {
    private final String rawHash;
    private final String encodedHash;

    public HashValue(HashingAlgorithm hashingAlgorithm, String rawHash) {
        this.rawHash = rawHash;
        this.encodedHash = hashingAlgorithm.toString() + ":" + rawHash;
    }

    public String encode() {
        return encodedHash;
    }

    public String decode() {
        return rawHash;
    }
}