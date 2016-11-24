package uk.gov.register.util;

import org.apache.commons.codec.digest.DigestUtils;
import uk.gov.register.core.HashingAlgorithm;

public class HashFactory {
    public String encode(HashingAlgorithm hashingAlgorithm, byte[] content) {
        return hashingAlgorithm.toString() + ":" + DigestUtils.sha256Hex(content);
    }
}