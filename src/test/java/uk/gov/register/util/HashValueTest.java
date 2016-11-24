package uk.gov.register.util;

import org.junit.Test;
import uk.gov.register.core.HashingAlgorithm;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class HashValueTest {
    @Test
    public void encode_shouldReturnPrependHashing() {
        HashValue hashValue = new HashValue(HashingAlgorithm.SHA256, "hash");
        String encodedHash = hashValue.encode();

        assertThat(encodedHash, equalTo(HashingAlgorithm.SHA256.toString() + ":hash"));
    }

    @Test
    public void getValue_shouldReturnValue() {
        HashValue hashValue = new HashValue(HashingAlgorithm.SHA256, "hash");
        String decodedHash = hashValue.getValue();

        assertThat(decodedHash, equalTo("hash"));
    }

    @Test
    public void decode_shouldReturnNewHashValue() {
        HashValue expectedHash = new HashValue(HashingAlgorithm.SHA256, "cc8a7c42275c84b94c6e282ae88b3dbcc06319156fc4539a2f39af053bf30592");
        String encodedHash = "sha-256:cc8a7c42275c84b94c6e282ae88b3dbcc06319156fc4539a2f39af053bf30592";

        HashValue actualHash = HashValue.decode(HashingAlgorithm.SHA256.toString(), encodedHash);

        assertThat(actualHash, equalTo(expectedHash));
    }

    @Test
    public void equal_shouldReturnTrueWhenBothEqual() {
        HashValue hash1 = new HashValue(HashingAlgorithm.SHA256, "cc8a7c42275c84b94c6e282ae88b3dbcc06319156fc4539a2f39af053bf30592");
        HashValue hash2 = new HashValue(HashingAlgorithm.SHA256, "cc8a7c42275c84b94c6e282ae88b3dbcc06319156fc4539a2f39af053bf30592");

        assertThat(hash1.equals(hash2), is(true));
    }

    @Test
    public void equal_shouldReturnFalseWhenNotEqual() {
        HashValue hash1 = new HashValue(HashingAlgorithm.SHA256, "cc8a7c42275c84b94c6e282ae88b3dbcc06319156fc4539a2f39af053bf30592");
        HashValue hash2 = new HashValue(HashingAlgorithm.SHA256, "dc8a7c42275c84b94c6e282ae88b3dbcc06319156fc4539a2f39af053bf30592");

        assertThat(hash1.equals(hash2), is(false));
    }
}