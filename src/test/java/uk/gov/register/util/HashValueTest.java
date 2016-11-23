package uk.gov.register.util;

import org.junit.Test;
import uk.gov.register.core.HashingAlgorithm;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class HashValueTest {
    @Test
    public void encode_shouldReturnPrependHashing() {
        HashValue hashValue = new HashValue(HashingAlgorithm.SHA256, "hash");
        String encodedHash = hashValue.encode();

        assertThat(encodedHash, equalTo(HashingAlgorithm.SHA256.toString() + ":hash"));
    }

    @Test
    public void decode_shouldReturnRawHash() {
        HashValue hashValue = new HashValue(HashingAlgorithm.SHA256, "hash");
        String decodedHash = hashValue.decode();

        assertThat(decodedHash, equalTo("hash"));
    }
}