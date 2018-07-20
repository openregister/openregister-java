package uk.gov.register.resources;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.ContentDisposition;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpServletResponseAdapter {
    private final HttpServletResponse httpServletResponse;

    public HttpServletResponseAdapter(HttpServletResponse httpServletResponse) {
        this.httpServletResponse = httpServletResponse;
    }

    public void addInlineContentDispositionHeader(String fileName) {
        ContentDisposition contentDisposition = ContentDisposition.type("inline").fileName(fileName).build();
        httpServletResponse.addHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
    }

    public void addLinkHeader(String rel, String value) {
        Map<String, String> extra = new HashMap<>();
        extra.put("rel", rel);
        addLinkHeader(extra, value);
    }

    public void addLinkHeader(Map<String, String> extra, String value) {
        String existingHeaderValue = httpServletResponse.getHeader("Link");

        String extraString = extra.entrySet()
                .stream()
                .map(entry -> entry.getKey() + "=\"" + entry.getValue() + "\"")
                .collect(Collectors.joining("; "));

        String newHeaderToAppend = String.format("<%s>; %s", value, extraString);

        String headerValue = StringUtils.isEmpty(existingHeaderValue) ? newHeaderToAppend : String.join(",", existingHeaderValue, newHeaderToAppend);

        setHeader("Link", headerValue);
    }

    public void setHeader(String name, String value) {
        httpServletResponse.setHeader(name, value);
    }
}
