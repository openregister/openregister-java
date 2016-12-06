package uk.gov.register;

import io.dropwizard.setup.Environment;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.servlet.ServletContainer;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.resourceresolver.FileResourceResolver;
import org.thymeleaf.templateresolver.TemplateResolver;
import uk.gov.register.core.AllTheRegisters;
import uk.gov.register.core.EverythingAboutARegister;
import uk.gov.register.core.RegisterData;
import uk.gov.register.thymeleaf.ThymeleafResourceResolver;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

public class AssetsBundleCustomErrorHandler extends ErrorHandler {
    private final Environment environment;
    private final TemplateEngine engine;

    public AssetsBundleCustomErrorHandler(Environment environment) {
        this.environment = environment;

        TemplateResolver templateResolver = new TemplateResolver();
        String baseDirForTemplates = System.getProperty("baseDirForTemplates");
        if (baseDirForTemplates != null) {
            templateResolver.setResourceResolver(new FileResourceResolver());
            templateResolver.setPrefix(baseDirForTemplates + "/templates/");
        } else {
            templateResolver.setResourceResolver(new ThymeleafResourceResolver());
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
        AllTheRegisters registers = sl.getService(AllTheRegisters.class);

        // FIXME this duplicates RequestContext.getHost() because we can't inject a RequestContext here
        String host = firstNonNull(request.getHeader("X-Forwarded-Host"),request.getHeader("Host"));
        String registerId = host.split("\\.")[0];
        EverythingAboutARegister register = registers.getRegisterByName(registerId);

        RegisterData rd = register.getRegisterData();
        String registerName = registerId.replace('-', ' ');

        WebContext wc = new WebContext(request, response, sc,
                request.getLocale());
        wc.setVariable("register", rd.getRegister());
        wc.setVariable("friendlyRegisterName", StringUtils.capitalize(registerName) + " register");
        wc.setVariable("renderedCopyrightText", rd.getRegister().getCopyright());

        response.setHeader("Content-Type", "text/html; charset=UTF-8");
        engine.process("404.html", wc, response.getWriter());
    }
}
