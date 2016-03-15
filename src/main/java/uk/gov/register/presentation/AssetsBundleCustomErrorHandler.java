package uk.gov.register.presentation;

import io.dropwizard.setup.Environment;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templateresolver.TemplateResolver;
import uk.gov.register.presentation.config.RegistersConfiguration;
import uk.gov.register.thymeleaf.ThymeleafResourceResolver;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class AssetsBundleCustomErrorHandler extends ErrorHandler {
    private final Logger LOGGER = LoggerFactory.getLogger(AssetsBundleCustomErrorHandler.class);
    private Environment environment;

    public AssetsBundleCustomErrorHandler(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        baseRequest.setHandled(true);

        TemplateResolver templateResolver = new TemplateResolver();
        templateResolver.setResourceResolver(new ThymeleafResourceResolver());
        templateResolver.setPrefix("/templates/");
        templateResolver.setTemplateMode("HTML5");
        templateResolver.setCacheable(false);
        templateResolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(templateResolver);

        ServletContext sc = baseRequest.getContext();
        ServiceLocator sl = ((ServletContainer) environment.getJerseyServletContainer()).getApplicationHandler().getServiceLocator();
        assert (sl != null);
        RegistersConfiguration rc = sl.getService(RegistersConfiguration.class);
        assert (rc != null);
        RegisterData rd = rc.getRegisterData(RegisterNameExtractor.extractRegisterName(request.getHeader("Host")));
        assert (rd != null);

        WebContext wc = new WebContext(request, response, sc,
                request.getLocale());
        wc.setVariable("register", rd.getRegister());

        engine.process("404.html", wc, response.getWriter());
    }
}
