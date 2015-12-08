package uk.gov.mint;

public class User implements java.security.Principal {
    @Override
    public String getName() {
        return "default user";
    }
}
