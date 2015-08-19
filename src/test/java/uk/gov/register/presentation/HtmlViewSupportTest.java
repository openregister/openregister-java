package uk.gov.register.presentation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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
    public void representationLink_1() throws URISyntaxException {
        when(servletRequest.getRequestURI()).thenReturn("/hash/hashvalue");
        when(servletRequest.getParameterMap()).thenReturn(Collections.emptyMap());
        String link = HtmlViewSupport.representationLink(servletRequest, "json");
        assertThat(link, equalTo("/hash/hashvalue.json"));
    }

    @Test
    public void representationLink_2() throws URISyntaxException {
        when(servletRequest.getRequestURI()).thenReturn("/hash/hashvalue.html");
        when(servletRequest.getParameterMap()).thenReturn(Collections.emptyMap());
        String link = HtmlViewSupport.representationLink(servletRequest, "json");
        assertThat(link, equalTo("/hash/hashvalue.json"));
    }

    @Test
    public void representationLink_3() throws URISyntaxException {
        when(servletRequest.getRequestURI()).thenReturn("/hash/hashvalue.csv");
        when(servletRequest.getParameterMap()).thenReturn(Collections.emptyMap());
        String link = HtmlViewSupport.representationLink(servletRequest, "csv");
        assertThat(link, equalTo("/hash/hashvalue.csv"));
    }

    @Test
    public void representationLink_4() throws URISyntaxException {
        when(servletRequest.getRequestURI()).thenReturn("/hash/hashvalue");
        when(servletRequest.getParameterMap()).thenReturn(Collections.singletonMap("query", new String[]{"value"}));
        String link = HtmlViewSupport.representationLink(servletRequest, "json");
        assertThat(link, equalTo("/hash/hashvalue.json?query=value"));
    }

    @Test
    public void representationLink_5() throws URISyntaxException {
        when(servletRequest.getRequestURI()).thenReturn("/hash/hashvalue.html");
        when(servletRequest.getParameterMap()).thenReturn(Collections.singletonMap("query", new String[]{"value"}));
        String link = HtmlViewSupport.representationLink(servletRequest, "json");
        assertThat(link, equalTo("/hash/hashvalue.json?query=value"));
    }

    @Test
    public void representationLink_6() throws URISyntaxException {
        when(servletRequest.getRequestURI()).thenReturn("/hash/hashvalue.csv");
        when(servletRequest.getParameterMap()).thenReturn(Collections.singletonMap("query", new String[]{"value"}));
        String link = HtmlViewSupport.representationLink(servletRequest, "csv");
        assertThat(link, equalTo("/hash/hashvalue.csv?query=value"));
    }
}
