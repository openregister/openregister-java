package uk.gov.register.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.exceptions.HashDecodeException;

public class HashValue {
    private final String value;
    private final String hashingAlgorithm;

    public HashValue(HashingAlgorithm hashingAlgorithm, String value) {
        this.hashingAlgorithm = hashingAlgorithm.toString();
        this.value = value;
    }

    public HashValue(String hashingAlgorithm, String value) {
        this.hashingAlgorithm = hashingAlgorithm;
        this.value = value;
    }

    @JsonValue
    public String encode() {
        return hashingAlgorithm + ":" + value;
    }

    @JsonIgnore
    public String getValue() {
        return value;
    }

    public static HashValue decode(HashingAlgorithm hashingAlgorithm, String encodedHash) {
        if (!encodedHash.startsWith(hashingAlgorithm.toString())) {
            throw new HashDecodeException(String.format("Hash \"%s\" has not been encoded with hashing algorithm \"%s\"", encodedHash, hashingAlgorithm));
        }

        String[] parts = encodedHash.split(hashingAlgorithm + ":");

        if (parts.length != 2) {
            throw new HashDecodeException(String.format("Cannot decode hash %s", encodedHash));
        }

        return new HashValue(hashingAlgorithm, parts[1]);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || o.getClass() != this.getClass()) return false;

        HashValue hashValue = (HashValue) o;

        if (value != null ? !value.equals(hashValue.value) : hashValue.value != null) return false;

        return hashingAlgorithm != null ? hashingAlgorithm.equals(hashValue.hashingAlgorithm) : hashValue.hashingAlgorithm == null;
    }

    @Override
    public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + (hashingAlgorithm != null ? hashingAlgorithm.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return encode();
    }
}