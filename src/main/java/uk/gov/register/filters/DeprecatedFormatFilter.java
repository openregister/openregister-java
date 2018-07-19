package uk.gov.register.filters;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.common.net.HttpHeaders;
import uk.gov.register.resources.HttpServletResponseAdapter;
import uk.gov.register.views.representations.ExtraMediaType;

@Provider
public class DeprecatedFormatFilter implements ContainerResponseFilter {

    @Context
    HttpServletResponse httpServletResponse;

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        final UriInfo uriInfo = requestContext.getUriInfo();
        final URI requestedUri = uriInfo.getRequestUri();
        final MultivaluedMap<String, Object> headers = responseContext.getHeaders();

        extractMediaType(headers).ifPresent(mediaType -> filterMediaType(mediaType, requestedUri, headers));
    }

    private void filterMediaType(MediaType mediaType, URI requestedUri, MultivaluedMap<String, Object> headers) {
        final String alternateUri;
        final String alternateType;

        if (recommendJson(mediaType)) {
            alternateUri = alternateLink(requestedUri, ".json");
            alternateType = MediaType.APPLICATION_JSON;
        } else if (recommendCsv(mediaType)) {
            alternateUri = alternateLink(requestedUri, ".csv");
            alternateType = ExtraMediaType.TEXT_CSV;
        } else {
            return;
        }

        HttpServletResponseAdapter httpServletResponseAdapter = new HttpServletResponseAdapter(httpServletResponse);

        Map<String, String> extra = new HashMap<>();
        extra.put("rel", "alternate");
        extra.put("type", alternateType);
        httpServletResponseAdapter.addLinkHeader(extra, alternateUri);

        headers.add(
                HttpHeaders.WARNING,
                warningHeader(mediaType.getSubtype())
        );
    }

    private String alternateLink(URI requestedUri, String recommendedExtension) {
        final String path = requestedUri.getPath();
        final UriBuilder builder = UriBuilder.fromUri(requestedUri);

        // Even if an extension was specified in the original URI, it has been stripped out
        // at this point, so just add the new one (before any trailing slash).
        final String newPath = path.replaceFirst("(/)?$", recommendedExtension + "$1");

        builder.replacePath(newPath);

        return builder.toTemplate();
    }

    private String warningHeader(String deprecatedType) {
        return "299 - \"Miscellaneous Persistent Warning\" \"" + deprecatedType + " is deprecated and will be removed. See \"Link\" header for a format to use instead.\"";
    }

    private boolean recommendJson(MediaType mediaType) {
        return mediaType.equals(ExtraMediaType.TEXT_TTL_TYPE) ||
                mediaType.equals(ExtraMediaType.TEXT_YAML_TYPE);
    }

    private boolean recommendCsv(MediaType mediaType) {
        return mediaType.equals(ExtraMediaType.TEXT_TSV_TYPE) ||
                mediaType.equals(ExtraMediaType.APPLICATION_SPREADSHEET_TYPE);
    }

    private Optional<MediaType> extractMediaType(MultivaluedMap<String, Object> headers) {
        try {
            MediaType mediaType = (MediaType) headers.getFirst(HttpHeaders.CONTENT_TYPE);
            return Optional.ofNullable(mediaType);
        } catch (ClassCastException e) {
            return Optional.empty();
        }
    }
}
