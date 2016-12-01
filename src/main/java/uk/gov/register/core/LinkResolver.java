package uk.gov.register.core;

import org.jvnet.hk2.annotations.Contract;

import java.net.URI;

@Contract
public interface LinkResolver {
    URI resolve(LinkValue linkValue);
}
