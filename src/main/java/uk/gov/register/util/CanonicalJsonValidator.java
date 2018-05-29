package uk.gov.register.util;

import uk.gov.register.exceptions.ItemNotCanonicalException;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class CanonicalJsonValidator {
    private final CanonicalJsonMapper canonicalJsonMapper;

    public CanonicalJsonValidator() {
        this.canonicalJsonMapper = new CanonicalJsonMapper();
    }

    public void validateItemStringIsCanonicalized(String jsonItem) {
        byte[] jsonItemBytes = jsonItem.getBytes(StandardCharsets.UTF_8);
        byte[] canonicalizedBytes = canonicalJsonMapper.writeToBytes(canonicalJsonMapper.readFromBytes(jsonItemBytes));

        if (!Arrays.equals(jsonItemBytes, canonicalizedBytes)) {
            throw new ItemNotCanonicalException(jsonItem);
        }
    }
}
