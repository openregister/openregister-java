package uk.gov.register.core;

import javax.inject.Inject;
import java.net.URI;

public class UriTemplateLinkResolver implements LinkResolver {
    private static final String recordTemplate = "/record/%s";
    private final RegisterResolver registerResolver;

    @Inject
    public UriTemplateLinkResolver(RegisterResolver registerResolver) {
        this.registerResolver = registerResolver;
    }

    @Override
    public URI resolve(String register, String linkKey) {
        URI baseUri = registerResolver.baseUriFor(register);

        String record = String.format(recordTemplate, linkKey);
        URI recordUri = URI.create(record);
        return baseUri.resolve(recordUri);
    }
}
