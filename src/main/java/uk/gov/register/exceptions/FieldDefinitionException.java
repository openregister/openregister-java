package uk.gov.register.exceptions;

/**
 * Used if there is a problem with the definition of a particular field.
 * For example, if the field's definition does not match the rest of the
 * environment.
 */
public class FieldDefinitionException extends RuntimeException {
	public FieldDefinitionException(String message) {
		super(message);
	}
}
