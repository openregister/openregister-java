package uk.gov.register.util;

import uk.gov.register.exceptions.BlobNotCanonicalException;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class CanonicalJsonValidator {
    private final CanonicalJsonMapper canonicalJsonMapper;

    public CanonicalJsonValidator() {
        this.canonicalJsonMapper = new CanonicalJsonMapper();
    }

    public void validateItemStringIsCanonicalized(String jsonItem) throws BlobNotCanonicalException {
        byte[] jsonItemBytes = jsonItem.getBytes(StandardCharsets.UTF_8);
        byte[] canonicalizedBytes = canonicalJsonMapper.writeToBytes(canonicalJsonMapper.readFromBytes(jsonItemBytes));

        if (!Arrays.equals(jsonItemBytes, canonicalizedBytes)) {
            throw new BlobNotCanonicalException(jsonItem);
        }
    }
}
