package uk.gov.register.util;

import uk.gov.register.exceptions.BlobNotCanonicalException;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class CanonicalJsonValidator {
    private final CanonicalJsonMapper canonicalJsonMapper;

    public CanonicalJsonValidator() {
        this.canonicalJsonMapper = new CanonicalJsonMapper();
    }

    public void validateBlobStringIsCanonicalized(String jsonBlob) throws BlobNotCanonicalException {
        byte[] jsonBlobBytes = jsonBlob.getBytes(StandardCharsets.UTF_8);
        byte[] canonicalizedBytes = canonicalJsonMapper.writeToBytes(canonicalJsonMapper.readFromBytes(jsonBlobBytes));

        if (!Arrays.equals(jsonBlobBytes, canonicalizedBytes)) {
            throw new BlobNotCanonicalException(jsonBlob);
        }
    }
}
