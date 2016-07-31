package uk.gov.register.thymeleaf;

import io.dropwizard.views.View;
import org.apache.commons.lang3.StringUtils;
import org.markdownj.MarkdownProcessor;
import uk.gov.register.presentation.RegisterData;
import uk.gov.register.configuration.Register;
import uk.gov.register.configuration.RegisterDomainConfiguration;
import uk.gov.register.resources.RequestContext;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class ThymeleafView extends View {
    protected final RequestContext requestContext;
    private final RegisterData registerData;
    private final String registerDomain;
    private String thymeleafTemplateName;
    protected final MarkdownProcessor markdownProcessor = new MarkdownProcessor();

    public ThymeleafView(RequestContext requestContext, String templateName, RegisterData registerData, RegisterDomainConfiguration registerDomainConfiguration) {
        super(templateName, StandardCharsets.UTF_8);
        this.requestContext = requestContext;
        this.registerData = registerData;
        this.registerDomain = registerDomainConfiguration.getRegisterDomain();
    }

    @Override
    public String getTemplateName() {

        if (null == thymeleafTemplateName) {
            thymeleafTemplateName = convertToThymeleafTemplateName(super.getTemplateName());
        }
        return thymeleafTemplateName;

    }

    private String convertToThymeleafTemplateName(String templateName) {
        String[] names = templateName.split("/");

        return names[names.length - 1];
    }

    @SuppressWarnings("unused, used by templates")
    public String getFriendlyRegisterName() {
        String registerId = getRegisterId();
        String registerName = registerId.replace('-',' ');
        return StringUtils.capitalize(registerName) + " register";
    }

    public String getRegisterId() {
        return registerData.getRegister().getRegisterName();
    }

    public Register getRegister() {
        return registerData.getRegister();
    }

    public Optional<String> getRenderedCopyrightText() {
        return getRegister().getCopyright().map(
                markdownProcessor::markdown
        );
    }

    public String getRegisterDomain() {
        return registerDomain;
    }

    public ServletContext getServletContext() {
        return requestContext.getServletContext();
    }

    public HttpServletResponse getHttpServletResponse() {
        return requestContext.getHttpServletResponse();
    }

    public HttpServletRequest getHttpServletRequest() {
        return requestContext.getHttpServletRequest();
    }

    @SuppressWarnings("unused, used by templates")
    public String getScheme() {
        return requestContext.getScheme();
    }
}
