package uk.gov.register.exceptions;

import uk.gov.register.util.HashValue;

/**
 * Used when a problem occurs validating the root hash of a register
 */
public class AssertRootHashException extends RuntimeException {

    public AssertRootHashException(HashValue expectedRootHash, HashValue actualRootHash) {
        this(String.format("Root hashes don't match. Expected: %s actual: %s", expectedRootHash.toString(), actualRootHash.toString()));
    }

    public AssertRootHashException(String message) {
        super(message);
    }

    public AssertRootHashException(String message, Throwable cause) {
        super(message, cause);
    }
}
