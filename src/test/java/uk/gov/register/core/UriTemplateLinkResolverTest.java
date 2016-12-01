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
        LinkValue linkValue = new LinkValue("address", "1111112");

        assertThat(localResolver.resolve(linkValue), is(URI.create("http://address.openregister.dev:8080/record/1111112")));
        assertThat(prodResolver.resolve(linkValue), is(URI.create("https://address.register.gov.uk/record/1111112")));
    }

    @Test
    public void curieValueReturnsCorrectLink() {
        LinkValue.CurieValue charityCurieValue = new LinkValue.CurieValue("charity:123456");
        LinkValue.CurieValue companyCurieValue = new LinkValue.CurieValue("company:654321");

        assertThat(localResolver.resolve(charityCurieValue), is(URI.create("http://charity.openregister.dev:8080/record/123456")));
        assertThat(localResolver.resolve(companyCurieValue), is(URI.create("http://company.openregister.dev:8080/record/654321")));
    }

    @Test
    public void separateRegisterAndValueReturnsCorrectLink() throws Exception {
        assertThat(localResolver.resolve("country","CZ"), is(URI.create("http://country.openregister.dev:8080/record/CZ")));
    }
}
