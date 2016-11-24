package uk.gov.register.exceptions;

import uk.gov.register.core.Entry;

public class NoSuchItemForEntryException extends RuntimeException {

    public NoSuchItemForEntryException(Entry entry) {
        super(String.format("No item found with item-hash: %s for entryNumber: %s and key: %s", entry.getItemHash(), entry.getEntryNumber(), entry.getKey()));
    }
}
