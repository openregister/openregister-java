package uk.gov.register.core;

import org.jvnet.hk2.annotations.Contract;

import java.net.URI;

@Contract
public interface LinkResolver {
    default URI resolve(LinkValue linkValue) {
        return resolve(linkValue.getTargetRegister(), linkValue.getLinkKey());
    }
    URI resolve(String register, String linkKey);
}
