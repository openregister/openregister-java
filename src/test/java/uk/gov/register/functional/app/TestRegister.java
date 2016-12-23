package uk.gov.register.functional.app;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

public enum TestRegister {
    address("foo", "bar"), postcode("pat", "goggins"), register("sasine", "inhibition");

    private static final String REGISTER_DOMAIN = "test.register.gov.uk";
    private final String hostname = name() + "." + REGISTER_DOMAIN;
    private final String username;
    private final String password;

    TestRegister(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getHostname() {
        return hostname;
    }

    public HttpAuthenticationFeature httpAuthFeature() {
        return HttpAuthenticationFeature.basicBuilder()
                .credentials(username, password)
                .build();
    }
}
