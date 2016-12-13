package uk.gov.register.core;

import org.junit.Test;

import java.net.URI;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class UriTemplateRegisterResolverTest {
    private final RegisterResolver localResolver = new UriTemplateRegisterResolver(() -> "http", () -> "openregister.dev:8080");
    private final RegisterResolver prodResolver = new UriTemplateRegisterResolver(() -> "https", () -> "register.gov.uk");

    @Test
    public void linkValueReturnsLink() {
        assertThat(localResolver.baseUriFor(new RegisterName("address")), is(URI.create("http://address.openregister.dev:8080")));
        assertThat(prodResolver.baseUriFor(new RegisterName("address")), is(URI.create("https://address.register.gov.uk")));
    }
}
