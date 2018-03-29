package uk.gov.register.core;

import java.net.URI;

public interface LinkResolver {
    URI resolve(RegisterId register, String linkKey);

    URI resolve(LinkValue linkValue);
}
