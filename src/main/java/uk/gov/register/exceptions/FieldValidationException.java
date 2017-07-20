package uk.gov.register.exceptions;

public class FieldValidationException extends RuntimeException {
	public FieldValidationException(String message) {
		super(message);
	}
}
