package uk.gov.register.core;

import org.junit.Test;

import java.net.URI;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class UriTemplateLinkResolverTest {

    private final UriTemplateLinkResolver localResolver = new UriTemplateLinkResolver(register -> URI.create("http://" + register + ".openregister.dev:8080"));
    private final UriTemplateLinkResolver prodResolver = new UriTemplateLinkResolver(register -> URI.create("https://" + register + ".register.gov.uk"));

    @Test
    public void linkValueReturnsLink() {
        RegisterLinkValue registerLinkValue = new RegisterLinkValue(new RegisterId("address"), "1111112");

        assertThat(localResolver.resolve(registerLinkValue), is(URI.create("http://address.openregister.dev:8080/records/1111112")));
        assertThat(prodResolver.resolve(registerLinkValue), is(URI.create("https://address.register.gov.uk/records/1111112")));
    }

    @Test
    public void urlValueReturnsLink() {
        UrlValue urlValue = new UrlValue("http://www.example.com");

        assertThat(localResolver.resolve(urlValue), is(URI.create("http://www.example.com")));
        assertThat(prodResolver.resolve(urlValue), is(URI.create("http://www.example.com")));
    }

    @Test
    public void curieValueReturnsCorrectLink() {
        RegisterLinkValue.CurieValue charityCurieValue = new RegisterLinkValue.CurieValue("charity:123456");
        RegisterLinkValue.CurieValue companyCurieValue = new RegisterLinkValue.CurieValue("company:654321");

        assertThat(localResolver.resolve(charityCurieValue), is(URI.create("http://charity.openregister.dev:8080/records/123456")));
        assertThat(localResolver.resolve(companyCurieValue), is(URI.create("http://company.openregister.dev:8080/records/654321")));
    }

    @Test
    public void separateRegisterAndValueReturnsCorrectLink() throws Exception {
        assertThat(localResolver.resolve(new RegisterId("country"), "CZ"), is(URI.create("http://country.openregister.dev:8080/records/CZ")));
    }

    @Test
    public void keysAreURLEncoded() throws Exception {
        assertThat(localResolver.resolve(new RegisterId("country"),"47.79/1"), is(URI.create("http://country.openregister.dev:8080/records/47.79%2F1")));
    }
}
