package uk.gov.register.util;

import uk.gov.register.core.HashingAlgorithm;

public class HashValue {
    private final String value;
    private final String hashingAlgorithm;

    public HashValue(HashingAlgorithm hashingAlgorithm, String value) {
        this.hashingAlgorithm = hashingAlgorithm.toString();
        this.value = value;
    }

    public String encode() {
        return hashingAlgorithm + ":" + value;
    }

    public String getValue() {
        return value;
    }

    public static HashValue decode(String hashingAlgorithm, String encodedHash) {
        if (!encodedHash.startsWith(hashingAlgorithm)) {
            throw new RuntimeException("Hash cannot be decoded");
        }

        String[] parts = encodedHash.split(hashingAlgorithm + ":");

        if (parts.length != 2) {
            throw new RuntimeException(String.format("Cannot create HashValue from encoded hash %s", encodedHash));
        }

        return new HashValue(HashingAlgorithm.SHA256, parts[1]);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || o.getClass() != this.getClass()) return false;

        HashValue that = (HashValue) o;

        return this.getValue().equals(that.getValue());
    }

    @Override
    public int hashCode() {
        return 31 * getValue().hashCode();
    }

    @Override
    public String toString() {
        return encode();
    }
}