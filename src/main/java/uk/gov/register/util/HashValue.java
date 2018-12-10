package uk.gov.register.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.exceptions.HashDecodeException;

public class HashValue {
    private final String value;
    private final HashingAlgorithm hashingAlgorithm;

    public HashValue(HashingAlgorithm hashingAlgorithm, String value) {
        this.hashingAlgorithm = hashingAlgorithm;
        this.value = value;
    }

    @JsonValue
    public String encode() {
        return hashingAlgorithm.toString() + value;
    }

    public String multihash() {
        return this.hashingAlgorithm.multihashPrefix() + value;
    }

    @JsonIgnore
    public String getValue() {
        return value;
    }

    public static HashValue decode(HashingAlgorithm hashingAlgorithm, String encodedHash) throws HashDecodeException {
        if (!encodedHash.startsWith(hashingAlgorithm.toString())) {
            throw new HashDecodeException(String.format("Hash \"%s\" has not been encoded with hashing algorithm \"%s\"", encodedHash, hashingAlgorithm));
        }

        String[] parts = encodedHash.split(hashingAlgorithm.toString());

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

        return hashingAlgorithm != null ? hashingAlgorithm.toString().equals(hashValue.hashingAlgorithm.toString()) : hashValue.hashingAlgorithm == null;
    }

    @Override
    public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + (hashingAlgorithm != null ? hashingAlgorithm.toString().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return encode();
    }
}
