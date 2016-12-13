package uk.gov.register.core;

import java.net.URI;

public interface RegisterResolver {
    URI baseUriFor(RegisterName name);

    default LinkResolver getLinkResolver() {
        return new UriTemplateLinkResolver(this);
    }
}
