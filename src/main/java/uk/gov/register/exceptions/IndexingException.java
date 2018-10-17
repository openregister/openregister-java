package uk.gov.register.exceptions;

import uk.gov.register.core.BaseEntry;

/**
 * Used when a problem occurs in indexing an entry
 */
public class IndexingException extends RuntimeException {
	public IndexingException(BaseEntry entry, String indexName, String message) {
		super(String.format("Failed to index entry #%s with key '%s' against index with name '%s': %s",
				entry.getEntryNumber(), entry.getKey(), indexName, message));
	}
}
