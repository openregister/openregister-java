package uk.gov.register.exceptions;

import java.io.FileNotFoundException;

public class NoSuchConfigException extends Exception {
    public NoSuchConfigException(FileNotFoundException e) {
        super(String.format("The specified external config file cannot be found: %s", e.getMessage()));
    }
}