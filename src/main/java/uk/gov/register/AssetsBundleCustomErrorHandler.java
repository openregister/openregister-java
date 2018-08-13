package uk.gov.register;

import io.dropwizard.setup.Environment;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.servlet.ServletContainer;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templateresolver.AbstractConfigurableTemplateResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.FileTemplateResolver;
import uk.gov.register.core.AllTheRegisters;
import uk.gov.register.core.RegisterContext;
import uk.gov.register.core.RegisterMetadata;
import uk.gov.register.core.RegisterId;
import uk.gov.register.db.Factories;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class AssetsBundleCustomErrorHandler extends ErrorHandler {
    private final Environment environment;
    private final TemplateEngine engine;

    public AssetsBundleCustomErrorHandler(Environment environment) {
        this.environment = environment;

        AbstractConfigurableTemplateResolver templateResolver;
        String baseDirForTemplates = System.getProperty("baseDirForTemplates");
        if (baseDirForTemplates != null) {
            templateResolver = new FileTemplateResolver();
            templateResolver.setPrefix(baseDirForTemplates + "/templates/");
        } else {
            templateResolver = new ClassLoaderTemplateResolver();
            templateResolver.setPrefix("/templates/");
        }
        templateResolver.setTemplateMode("HTML5");
        templateResolver.setCacheable(false);
        templateResolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        engine = new TemplateEngine();
        engine.setTemplateResolver(templateResolver);
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (response.getStatus() != HttpServletResponse.SC_NOT_FOUND) {
            return;
        }

        baseRequest.setHandled(true);

        ServletContext sc = baseRequest.getContext();
        ServiceLocator sl = ((ServletContainer) environment.getJerseyServletContainer()).getApplicationHandler().getServiceLocator();
        AllTheRegisters allTheRegisters = sl.getService(AllTheRegisters.class);
        // We can't get the RegisterContext from the ServiceLocator because we're no longer in the request scope
        // so we have to manually new up the factory
        RegisterContext register = new Factories.RegisterContextProvider(allTheRegisters, () -> request).provide();

        RegisterId registerId = register.getRegisterId();

        RegisterMetadata rm = register.getRegisterMetadata();

        WebContext wc = new WebContext(request, response, sc,
                request.getLocale());
        wc.setVariable("register", rm);
        wc.setVariable("friendlyRegisterName", registerId.getFriendlyRegisterName() + " register");
        wc.setVariable("renderedCopyrightText", Optional.ofNullable(rm.getCopyright()));
        wc.setVariable("heading", "Page not found");
        wc.setVariable("message", "If you entered a web address please check it was correct.");

        response.setHeader("Content-Type", "text/html; charset=UTF-8");
        engine.process("exception.html", wc, response.getWriter());
    }

}
