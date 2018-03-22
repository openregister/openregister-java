package uk.gov.register.core;

import java.net.URI;

public interface LinkResolver {
    URI resolve(RegisterName register, String linkKey);

    URI resolve(LinkValue linkValue);
}
