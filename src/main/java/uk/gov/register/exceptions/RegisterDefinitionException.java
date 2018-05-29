package uk.gov.register.exceptions;

import uk.gov.register.core.RegisterId;

/**
 * Used if there is a problem with the definition of a particular register.
 * For example, if the register's definition does not match the rest of the
 * environment.
 */
public class RegisterDefinitionException extends RuntimeException {
	public RegisterDefinitionException(RegisterId registerId) {
		super("Definition of register " + registerId.value() + " does not match Register Register");
	}
}
