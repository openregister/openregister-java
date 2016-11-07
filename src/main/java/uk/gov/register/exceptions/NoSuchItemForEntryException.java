package uk.gov.register.exceptions;

public class NoSuchItemForEntryException extends RuntimeException {

    public NoSuchItemForEntryException(String itemHash, int entryNumber) {
        super(String.format("No item found with item-hash %s for entryNumber %s", itemHash, entryNumber));
    }
}
