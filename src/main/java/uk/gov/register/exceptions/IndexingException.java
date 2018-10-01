package uk.gov.register.exceptions;

import uk.gov.register.core.Entry;

/**
 * Used when a problem occurs in indexing an entry
 */
public class IndexingException extends RuntimeException {
	public IndexingException(Entry entry, String message) {
		super(String.format("Failed to index entry #%s with key '%s': %s",
				entry.getEntryNumber(), entry.getKey(), message));
	}
}
