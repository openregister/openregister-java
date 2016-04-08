package uk.gov.register.presentation.resource;

import org.glassfish.jersey.media.multipart.ContentDisposition;

import javax.ws.rs.core.HttpHeaders;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//TODO: temporary class to reuse same functionality required, this will be refactored once the old resource code is deleted
class ResourceCommon{

    protected final RequestContext requestContext;

    ResourceCommon(RequestContext requestContext) {
        this.requestContext = requestContext;
    }

    protected Optional<String> getFileExtension() {
        String requestURI = requestContext.getHttpServletRequest().getRequestURI();
        if (requestURI.lastIndexOf('.') == -1) {
            return Optional.empty();
        }
        String[] tokens = requestURI.split("\\.");
        return Optional.of(tokens[tokens.length - 1]);
    }

    protected void addContentDispositionHeader(String fileName) {
        ContentDisposition contentDisposition = ContentDisposition.type("attachment").fileName(fileName).build();
        requestContext.getHttpServletResponse().addHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
    }

    protected void setNextAndPreviousPageLinkHeader(Pagination pagination) {
        List<String> headerValues = new ArrayList<>();

        if (pagination.hasNextPage()) {
            headerValues.add(String.format("<%s>; rel=\"%s\"", pagination.getNextPageLink(), "next"));
        }

        if (pagination.hasPreviousPage()) {
            headerValues.add(String.format("<%s>; rel=\"%s\"", pagination.getPreviousPageLink(), "previous"));
        }

        if (!headerValues.isEmpty()) {
            requestContext.getHttpServletResponse().setHeader("Link", String.join(",", headerValues));
        }
    }
}
