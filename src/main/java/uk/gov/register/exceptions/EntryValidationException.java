package uk.gov.register.exceptions;

import uk.gov.register.core.Entry;

public class EntryValidationException extends RuntimeException {
	public EntryValidationException(Entry entry, String message) { 
		super("Entry #" + entry.getEntryNumber() + " with key " + entry.getKey() + " - " + message); 
	}
}
