package uk.gov.register.core;

import java.net.URI;

public interface LinkResolver {
    default URI resolve(LinkValue linkValue) {
        return resolve(linkValue.getTargetRegister(), linkValue.getLinkKey());
    }
    URI resolve(RegisterName register, String linkKey);

    URI resolve(UrlValue urlValue);
}
