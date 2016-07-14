package uk.gov.register.presentation.resource;

import org.jvnet.hk2.annotations.Service;
import uk.gov.register.presentation.RegisterData;
import uk.gov.register.presentation.RegisterNameExtractor;
import uk.gov.register.presentation.config.Register;
import uk.gov.register.presentation.config.RegisterDomainConfiguration;
import uk.gov.register.presentation.config.RegistersConfiguration;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import java.util.Optional;

@Service
public class RequestContext {

    @Context
    HttpServletRequest httpServletRequest;

    @Context
    ServletContext servletContext;

    @Context
    HttpServletResponse httpServletResponse;

    private final RegisterData registerData;

    private final String registerDomain;

    @Inject
    public RequestContext(RegisterData registerData, RegisterDomainConfiguration domainConfiguration) {
        this.registerData = registerData;
        this.registerDomain = domainConfiguration.getRegisterDomain();
    }

    public String getRegisterPrimaryKey() {
        return registerData.getRegister().getRegisterName();
    }

    public String getScheme() {
        return httpServletRequest.getScheme();
    }

    public HttpServletRequest getHttpServletRequest() {
        return httpServletRequest;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    public HttpServletResponse getHttpServletResponse() {
        return httpServletResponse;
    }

    public Register getRegister() {
        return registerData.getRegister();
    }

    public RegisterData getRegisterData() {
        return registerData;
    }

    public String getRegisterDomain() {
        return registerDomain;
    }

    public Optional<String> resourceExtension() {
        String requestURI = httpServletRequest.getRequestURI();
        if (requestURI.lastIndexOf('.') == -1) {
            return Optional.empty();
        }
        String[] tokens = requestURI.split("\\.");
        return Optional.of(tokens[tokens.length - 1]);
    }
}
