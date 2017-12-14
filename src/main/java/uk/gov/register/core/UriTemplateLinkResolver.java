package uk.gov.register.core;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class UriTemplateLinkResolver implements LinkResolver {
    private final RegisterResolver registerResolver;

    public UriTemplateLinkResolver(RegisterResolver registerResolver) {
        this.registerResolver = registerResolver;
    }

    // OGNL (Thymeleaf's language) can't find default methods by reflection
    @Override
    public URI resolve(LinkValue linkValue) {
        return LinkResolver.super.resolve(linkValue);
    }

    @Override
    public URI resolve(RegisterName register, String linkKey) {
        URI baseUri = registerResolver.baseUriFor(register);

        return UriBuilder.fromUri(baseUri).path("record").path(linkKey).build();
    }

    @Override
    public URI resolve(UrlValue urlValue) {
        return URI.create(urlValue.getValue());
    }
}
