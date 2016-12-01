package uk.gov.register.core;

import uk.gov.register.configuration.RegisterDomainConfiguration;
import uk.gov.register.resources.SchemeContext;

import javax.inject.Inject;
import java.net.URI;

public class UriTemplateRegisterResolver implements RegisterResolver {
    private static final String template = "%s://%s.%s";
    private final SchemeContext schemeContext;
    private final String registerDomain;

    @Inject
    public UriTemplateRegisterResolver(SchemeContext schemeContext, RegisterDomainConfiguration registerDomainConfiguration) {
        this.schemeContext = schemeContext;
        this.registerDomain = registerDomainConfiguration.getRegisterDomain();
    }

    @Override
    public URI baseUriFor(String name) {
        return URI.create(String.format(template, schemeContext.getScheme(), name, registerDomain));
    }
}
