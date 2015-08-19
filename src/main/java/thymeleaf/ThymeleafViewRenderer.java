package thymeleaf;

import io.dropwizard.views.View;
import io.dropwizard.views.ViewRenderer;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.TemplateResolver;

import javax.ws.rs.WebApplicationException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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

        TemplateResolver templateResolver = new TemplateResolver();
        templateResolver.setResourceResolver(new ThymeleafResourceResolver());
        templateResolver.setTemplateMode(templateMode);
        templateResolver.setPrefix(prefix);
        templateResolver.setCacheable(cacheable);
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
        OutputStreamWriter writer = new OutputStreamWriter(output);
        engine.process(view.getTemplateName(), context, writer);
        writer.flush();
    }

    @Override
    public void configure(Map<String, String> options) {

    }

    @Override
    public String getSuffix() {
        return suffix;
    }
}
