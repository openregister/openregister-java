package uk.gov.register.thymeleaf;

import io.dropwizard.views.View;
import org.apache.commons.lang3.StringUtils;
import uk.gov.register.presentation.config.PublicBodiesConfiguration;
import uk.gov.register.presentation.config.PublicBody;
import uk.gov.register.presentation.config.Register;
import uk.gov.register.presentation.resource.RequestContext;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

public class ThymeleafView extends View {

    protected final PublicBodiesConfiguration publicBodiesConfiguration;
    private final RequestContext requestContext;

    private String thymeleafTemplateName;

    public ThymeleafView(RequestContext requestContext, PublicBodiesConfiguration publicBodiesConfiguration, String templateName) {
        super(templateName, StandardCharsets.UTF_8);
        this.requestContext = requestContext;
        this.publicBodiesConfiguration = publicBodiesConfiguration;
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
        return requestContext.getRegisterPrimaryKey();
    }

    public Register getRegister() {
        return requestContext.getRegister();
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
    public PublicBody getCustodian() {
        return publicBodiesConfiguration.getPublicBody(getRegister().getRegistry());
    }
}

