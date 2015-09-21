package uk.gov.register.thymeleaf;

import io.dropwizard.views.View;
import org.apache.commons.lang3.StringUtils;
import uk.gov.register.presentation.resource.RequestContext;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.Charset;

public class ThymeleafView extends View {

    private final RequestContext requestContext;

    private String thymeleafTemplateName;

    public ThymeleafView(RequestContext requestContext, String templateName) {
        super(templateName, Charset.forName("UTF-8"));
        this.requestContext = requestContext;
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

    public ServletContext getServletContext() {
        return requestContext.getServletContext();
    }

    public HttpServletResponse getHttpServletResponse() {
        return requestContext.getHttpServletResponse();
    }

    public HttpServletRequest getHttpServletRequest() {
        return requestContext.getHttpServletRequest();
    }


}
