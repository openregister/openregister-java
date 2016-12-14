package uk.gov.register.exceptions;

public class NoSuchConfigException extends Exception {
    public NoSuchConfigException(String directory, String configFileName) {
        super(String.format("The specified config file %s does not exist in directory %s", configFileName, directory));
    }
}