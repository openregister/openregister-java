package uk.gov.register.core;

import java.net.URI;

public interface RegisterResolver {
    URI baseUriFor(String name);
}
