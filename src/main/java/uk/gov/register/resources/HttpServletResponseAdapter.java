package uk.gov.register.resources;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.ContentDisposition;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;

class HttpServletResponseAdapter {
    private final HttpServletResponse httpServletResponse;

    HttpServletResponseAdapter(HttpServletResponse httpServletResponse) {
        this.httpServletResponse = httpServletResponse;
    }

    void addInlineContentDispositionHeader(String fileName) {
        ContentDisposition contentDisposition = ContentDisposition.type("inline").fileName(fileName).build();
        httpServletResponse.addHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
    }

    void addLinkHeader(String rel, String value) {
        String existingHeaderValue = httpServletResponse.getHeader("Link");
        String newHeaderToAppend = String.format("<%s>; rel=\"%s\"", value, rel);

        String headerValue = StringUtils.isEmpty(existingHeaderValue) ? newHeaderToAppend : String.join(",", existingHeaderValue, newHeaderToAppend);

        httpServletResponse.setHeader("Link", headerValue);
    }
}
