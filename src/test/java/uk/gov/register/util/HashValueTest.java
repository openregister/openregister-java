package uk.gov.register.util;

import org.junit.Test;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.exceptions.HashDecodeException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

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

        HashValue actualHash = HashValue.decode(HashingAlgorithm.SHA256, encodedHash);

        assertThat(actualHash, equalTo(expectedHash));
    }

    @Test(expected = HashDecodeException.class)
    public void decode_shouldThrowExceptionWhenHashingAlgorithmDoesNotMatch() {
        String encodedHash = "sha-256notValid:cc8a7c42275c84b94c6e282ae88b3dbcc06319156fc4539a2f39af053bf30592";

        HashValue.decode(HashingAlgorithm.SHA256, encodedHash);
    }

    @Test(expected = HashDecodeException.class)
    public void decode_shouldThrowExceptionWhenEmptyString() {
        String encodedHash = "";

        HashValue.decode(HashingAlgorithm.SHA256, encodedHash);
    }

    @Test
    public void equal_shouldReturnTrueWhenBothEqual() {
        HashValue hash1 = new HashValue(HashingAlgorithm.SHA256, "cc8a7c42275c84b94c6e282ae88b3dbcc06319156fc4539a2f39af053bf30592");
        HashValue hash2 = new HashValue(HashingAlgorithm.SHA256, "cc8a7c42275c84b94c6e282ae88b3dbcc06319156fc4539a2f39af053bf30592");

        assertThat(hash1.equals(hash2), is(true));
    }

    @Test
    public void equal_shouldReturnFalseWhenHashNotEqual() {
        HashValue hash1 = new HashValue(HashingAlgorithm.SHA256, "cc8a7c42275c84b94c6e282ae88b3dbcc06319156fc4539a2f39af053bf30592");
        HashValue hash2 = new HashValue(HashingAlgorithm.SHA256, "dc8a7c42275c84b94c6e282ae88b3dbcc06319156fc4539a2f39af053bf30592");

        assertThat(hash1.equals(hash2), is(false));
    }

    @Test
    public void equal_shouldReturnFalseWhenHashingAlgorithmNotEqual() {
        HashValue hash1 = new HashValue(HashingAlgorithm.SHA256, "cc8a7c42275c84b94c6e282ae88b3dbcc06319156fc4539a2f39af053bf30592");
        HashValue hash2 = new HashValue("md5", "cc8a7c42275c84b94c6e282ae88b3dbcc06319156fc4539a2f39af053bf30592");

        assertThat(hash1.equals(hash2), is(false));
    }

    @Test
    public void hashCode_shouldBeEqual_whenBothAreEqual() {
        HashValue hash1 = new HashValue(HashingAlgorithm.SHA256, "cc8a7c42275c84b94c6e282ae88b3dbcc06319156fc4539a2f39af053bf30592");
        HashValue hash2 = new HashValue(HashingAlgorithm.SHA256, "cc8a7c42275c84b94c6e282ae88b3dbcc06319156fc4539a2f39af053bf30592");

        assertThat(hash1.hashCode(), equalTo(hash2.hashCode()));
    }

    @Test
    public void hashCode_shouldNotBeEqual_whenHashingAlgorithmNotEqual() {
        HashValue hash1 = new HashValue("sha-256", "cc8a7c42275c84b94c6e282ae88b3dbcc06319156fc4539a2f39af053bf30592");
        HashValue hash2 = new HashValue("md5", "cc8a7c42275c84b94c6e282ae88b3dbcc06319156fc4539a2f39af053bf30592");

        assertThat(hash1.hashCode(), not(hash2.hashCode()));
    }

    @Test
    public void hashCode_shouldNotBeEqual_whenHashNotEqual() {
        HashValue hash1 = new HashValue(HashingAlgorithm.SHA256, "cc8a7c42275c84b94c6e282ae88b3dbcc06319156fc4539a2f39af053bf30592");
        HashValue hash2 = new HashValue(HashingAlgorithm.SHA256, "dc8a7c42275c84b94c6e282ae88b3dbcc06319156fc4539a2f39af053bf30592");

        assertThat(hash1.hashCode(), not(hash2.hashCode()));
    }
}
