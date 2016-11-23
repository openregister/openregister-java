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

    public static String decode(String hashingAlgorithm, String encodedHash) {
        if (!encodedHash.startsWith(hashingAlgorithm)) {
            throw new RuntimeException("Hash cannot be decoded");
        }

        String[] parts = encodedHash.split(hashingAlgorithm + ":");
        return parts[1];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || o.getClass() != this.getClass()) return false;

        HashValue that = (HashValue) o;

        return this.getValue().equals(that.getValue());
    }

    @Override
    public String toString() {
        return getValue();
    }
}