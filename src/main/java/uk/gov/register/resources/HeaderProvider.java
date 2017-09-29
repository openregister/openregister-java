package uk.gov.register.resources;

import javax.inject.Inject;
import java.util.Objects;

public class HeaderProvider {

    private final RequestContext requestContext;
    private final HttpServletResponseAdapter httpServletResponseAdapter;

    @Inject
    public HeaderProvider(final RequestContext requestContext, final HttpServletResponseAdapter httpServletResponseAdapter) {
        this.requestContext = requestContext;
        this.httpServletResponseAdapter = httpServletResponseAdapter;
    }

    public void setInlineContentDisposition(final String partialName, final String type) {
        requestContext.resourceExtension().ifPresent(
                ext -> httpServletResponseAdapter.addInlineContentDispositionHeader(
                        getFileName(partialName, type, ext)
                )
        );
    }

    public void setAttachmentContentDisposition(final String partialName) {
        setAttachmentContentDisposition(partialName, null);
    }

    public void setAttachmentContentDisposition(final String partialName, final String type) {
        requestContext.resourceExtension().ifPresent(
                ext -> httpServletResponseAdapter.addAttachmentContentDispositionHeader(
                        getFileName(partialName, type, ext)
                )
        );
    }

    private String getFileName(final String partialName, final String type, final String ext) {
        return Objects.isNull(type)
                ? (String.format("%s.%s", partialName, ext))
                : (String.format("%s-%s.%s", partialName, type, ext));
    }
}
