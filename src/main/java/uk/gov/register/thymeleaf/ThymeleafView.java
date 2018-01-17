package uk.gov.register.thymeleaf;

import io.dropwizard.views.View;
import org.markdownj.MarkdownProcessor;
import uk.gov.register.core.*;
import uk.gov.register.resources.RequestContext;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ThymeleafView extends View {
    protected final RequestContext requestContext;
    private final RegisterResolver registerResolver;
    private final RegisterReadOnly register;
    private String thymeleafTemplateName;
    protected final MarkdownProcessor markdownProcessor = new MarkdownProcessor();

    public ThymeleafView(final RequestContext requestContext, final String templateName, final RegisterResolver registerResolver, final RegisterReadOnly register) {
        super(templateName, StandardCharsets.UTF_8);
        this.requestContext = requestContext;
        this.registerResolver = registerResolver;
        this.register = register;
    }

    @Override
    public String getTemplateName() {

        if (null == thymeleafTemplateName) {
            thymeleafTemplateName = convertToThymeleafTemplateName(super.getTemplateName());
        }
        return thymeleafTemplateName;

    }

    private String convertToThymeleafTemplateName(final String templateName) {
        final String[] names = templateName.split("/");

        return names[names.length - 1];
    }

    @SuppressWarnings("unused, used by templates")
    public String getFriendlyRegisterName() {
        // FIXME: this string concat should be in the template?
        return getRegisterId().getFriendlyRegisterName() + " register";
    }

    public RegisterName getRegisterId() {
        return register.getRegisterName();
    }

    public RegisterMetadata getRegister() {
        return register.getRegisterMetadata();
    }

    @SuppressWarnings("unused, used by templates")
    public LinkResolver getLinkResolver() {
        return getRegisterResolver().getLinkResolver();
    }

    public RegisterResolver getRegisterResolver() {
        return registerResolver;
    }

    public Optional<String> getRenderedCopyrightText() {
        return Optional.ofNullable(getRegister().getCopyright()).map(
                markdownProcessor::markdown
        );
    }

    @SuppressWarnings("unused, used by templates")
    public List<String> getOrderedFieldNames() {
        final List<String> orderedFieldNames = new ArrayList<>();
        getRegister().getPrimaryKeyField().ifPresent(orderedFieldNames::add);
        getRegister().getNonPrimaryFields().forEach(orderedFieldNames::add);
        return orderedFieldNames;
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

    @SuppressWarnings("unused, used by templates")
    public Boolean getIsGovukBranded() {
        return requestContext.getHost().endsWith("register.gov.uk");
    }
}
