package uk.gov.register.exceptions;

import uk.gov.register.core.RegisterName;

public class RegisterValidationException extends RuntimeException {
	public RegisterValidationException(RegisterName registerName) {
		super("Definition of register " + registerName.value() + " does not match Register Register");
	}
}
