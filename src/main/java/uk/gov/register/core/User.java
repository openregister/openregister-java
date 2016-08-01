package uk.gov.register.core;

public class User implements java.security.Principal {
    @Override
    public String getName() {
        return "default user";
    }
}
