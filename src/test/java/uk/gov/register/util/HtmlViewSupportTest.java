package uk.gov.register.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import javax.servlet.http.HttpServletRequest;
import java.net.URISyntaxException;
import java.util.Collections;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HtmlViewSupportTest {
    @Mock
    HttpServletRequest servletRequest;

    @Test
    public void representationLink_addTheRepresentationSuffixForNonHtmlRepresentation() throws URISyntaxException {
        when(servletRequest.getRequestURI()).thenReturn("/item/hashvalue");
        when(servletRequest.getParameterMap()).thenReturn(Collections.singletonMap("query", new String[]{"value"}));
        String link = HtmlViewSupport.representationLink(servletRequest, "json");
        assertThat(link, equalTo("/item/hashvalue.json?query=value"));
    }

    @Test
    public void representationLink_maintainTheSameRepresentationSuffix() throws URISyntaxException {
        when(servletRequest.getRequestURI()).thenReturn("/item/hashvalue.csv");
        when(servletRequest.getParameterMap()).thenReturn(Collections.singletonMap("query", new String[]{"value"}));
        String link = HtmlViewSupport.representationLink(servletRequest, "csv");
        assertThat(link, equalTo("/item/hashvalue.csv?query=value"));
    }
}
