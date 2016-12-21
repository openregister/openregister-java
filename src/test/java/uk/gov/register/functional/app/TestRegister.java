package uk.gov.register.functional.app;

public enum TestRegister {
    address, postcode, register;

    private static final String REGISTER_DOMAIN = "test.register.gov.uk";
    private final String hostname = name() + "." + REGISTER_DOMAIN;

    public String getHostname() {
        return hostname;
    }
}
