package uk.gov.register.thymeleaf;

import io.dropwizard.views.View;
import io.dropwizard.views.ViewRenderer;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.AbstractConfigurableTemplateResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import javax.ws.rs.WebApplicationException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

public class ThymeleafViewRenderer implements ViewRenderer {

    private final TemplateEngine engine;
    private final String suffix;

    public ThymeleafViewRenderer(
            String templateMode,
            String prefix,
            String suffix,
            boolean cacheable) {
        super();

        this.suffix = suffix;

        AbstractConfigurableTemplateResolver templateResolver;
        String baseDirForTemplates = System.getProperty("baseDirForTemplates");
        if (baseDirForTemplates != null) {
            templateResolver = new FileTemplateResolver();
            templateResolver.setPrefix(baseDirForTemplates + prefix);
        } else {
            templateResolver = new ClassLoaderTemplateResolver();
            templateResolver.setPrefix(prefix);
        }

        templateResolver.setTemplateMode(templateMode);
        templateResolver.setCacheable(cacheable);
        templateResolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        engine = new TemplateEngine();
        engine.setTemplateResolver(templateResolver);

    }

    @Override
    public boolean isRenderable(View view) {
        return view.getTemplateName().endsWith(suffix);
    }

    @Override
    public void render(View view, Locale locale, OutputStream output)
            throws IOException, WebApplicationException {

        ThymeleafContext context = new ThymeleafContext((ThymeleafView) view);
        OutputStreamWriter writer = new OutputStreamWriter(output, StandardCharsets.UTF_8);
        engine.process(view.getTemplateName(), context, writer);
        writer.flush();
    }

    @Override
    public void configure(Map<String, String> options) {

    }

    @Override
    public String getConfigurationKey() {
        return "freemarker";
    }
}
