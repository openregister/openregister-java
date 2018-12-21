package uk.gov.register.service;

public class EntryValidator {
    public static void validateKey(String key)
    {
        if(!key.matches("[A-Za-z\\d][A-Za-z\\d-_./]*")) {
            throw new RuntimeException("Key is not valid: " + key);
        }

        if(key.matches(".*[-_./]{2}.*")) {
            throw new RuntimeException("Key is invalid due to consecutive restricted characters: " + key);
        }
    }
}
