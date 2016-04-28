package uk.gov.register.presentation;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class LinkValueTest {

    @Test
    public void linkValueReturnsLink() {
        LinkValue httpLinkValue = new LinkValue("address", "test.openregister.org", "http", "1111111");
        LinkValue httpsLinkValue = new LinkValue("address", "register.gov.uk", "https", "1111112");

        assertThat(httpLinkValue.link(), is("http://address.test.openregister.org/record/1111111"));
        assertThat(httpsLinkValue.link(), is("https://address.register.gov.uk/record/1111112"));
    }

    @Test
    public void curieValueReturnsCorrectLink() {
        LinkValue.CurieValue httpCurieValue = new LinkValue.CurieValue("charity:123456", "test.openregister.org", "http");
        LinkValue.CurieValue httpsCurieValue = new LinkValue.CurieValue("company:654321", "register.gov.uk", "https");

        assertThat(httpCurieValue.link(), is("http://charity.test.openregister.org/record/123456"));
        assertThat(httpsCurieValue.link(), is("https://company.register.gov.uk/record/654321"));
    }
}
