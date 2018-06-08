package uk.gov.register.core;

import javax.ws.rs.core.UriBuilder;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class UriTemplateLinkResolver implements LinkResolver {
    private final RegisterResolver registerResolver;

    public UriTemplateLinkResolver(RegisterResolver registerResolver) {
        this.registerResolver = registerResolver;
    }

    @Override
    public URI resolve(LinkValue linkValue) {
        if (linkValue.isLinkToRegister()) {
            RegisterLinkValue registerLinkValue = (RegisterLinkValue) linkValue;
            return resolve(registerLinkValue.getTargetRegister(), registerLinkValue.getLinkKey());
        } else {
            return URI.create(linkValue.getValue());
        }
    }

    public URI resolve(RegisterId register, String linkKey) {
        URI baseUri = registerResolver.baseUriFor(register);

        try {
            return UriBuilder.fromUri(baseUri).path("records").path(URLEncoder.encode(linkKey, StandardCharsets.UTF_8.name())).build();
        } catch (UnsupportedEncodingException e) {
            return UriBuilder.fromUri(baseUri).path("records").path(linkKey).build();
        }
    }
}
