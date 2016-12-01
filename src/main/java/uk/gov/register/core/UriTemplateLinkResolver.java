package uk.gov.register.core;

import uk.gov.register.configuration.RegisterDomainConfiguration;
import uk.gov.register.resources.SchemeContext;

import javax.inject.Inject;
import java.net.URI;

public class UriTemplateLinkResolver implements LinkResolver {
    private static final String template = "%1$s://%2$s.%3$s/record/%4$s";
    private final SchemeContext schemeContext;
    private final String registerDomain;

    @Inject
    public UriTemplateLinkResolver(SchemeContext schemeContext, RegisterDomainConfiguration registerDomainConfiguration) {
        this.schemeContext = schemeContext;
        this.registerDomain = registerDomainConfiguration.getRegisterDomain();
    }

    @Override
    public URI resolve(LinkValue linkValue) {
        return URI.create(String.format(template, schemeContext.getScheme(), linkValue.getTargetRegister(), registerDomain, linkValue.getLinkKey()));
    }
}
