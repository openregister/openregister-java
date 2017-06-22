package uk.gov.register.functional.app;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

public enum TestRegister {
    address("foo", "bar"), postcode("pat", "goggins"), register("sasine", "inhibition"), local_authority_eng("bar","baz");

    private static final String REGISTER_DOMAIN = "test.register.gov.uk";
    private final String hostname = getSchema() + "." + REGISTER_DOMAIN;
    private final String username;
    private final String password;
    private final String databaseConnectionString = "jdbc:postgresql://localhost:5432/ft_openregister_java_multi?user=postgres&ApplicationName=%s";

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

    public String getDatabaseConnectionString(String applicationName) {
        return String.format(databaseConnectionString, applicationName);
    }

    public String getSchema() {
        return name().replaceAll("_","-");
    }

}
