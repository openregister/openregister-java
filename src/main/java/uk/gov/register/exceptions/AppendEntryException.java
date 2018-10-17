package uk.gov.register.exceptions;

import uk.gov.register.core.Entry;

/**
 * Used when a problem occurs appending a new entry to a register
 */
public class AppendEntryException extends RuntimeException {

    public AppendEntryException(Entry entry) {
        this(generateMessage(entry));
    }

    public AppendEntryException(String message) {
        super(message);
    }

    public AppendEntryException(Entry entry, Throwable cause) {
        this(generateMessage(entry), cause);
    }

    public AppendEntryException(String message, Throwable cause) {
        super(message, cause);
    }

    private static String generateMessage(Entry entry) {
        return String.format("Failed to append entry with entry-number %s and key '%s'", entry.getEntryNumber(), entry.getKey());
    }
}
