package uk.gov.register.exceptions;

/**
 * Used if the register's state is inconsistent with the rest of the environment
 */
public class EnvironmentInconsistencyException extends RuntimeException {

    public EnvironmentInconsistencyException(final String message) {
        super(message);
    }
}
