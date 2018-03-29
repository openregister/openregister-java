package uk.gov.register.exceptions;

import uk.gov.register.core.RegisterId;

public class RegisterValidationException extends RuntimeException {
	public RegisterValidationException(RegisterId registerId) {
		super("Definition of register " + registerId.value() + " does not match Register Register");
	}
}
