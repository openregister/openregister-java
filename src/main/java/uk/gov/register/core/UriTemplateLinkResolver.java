package uk.gov.register.core;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

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

        return UriBuilder.fromUri(baseUri).path("record").path(linkKey).build();
    }
}
