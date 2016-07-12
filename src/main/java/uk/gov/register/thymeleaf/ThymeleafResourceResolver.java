package uk.gov.register.thymeleaf;

import org.thymeleaf.TemplateProcessingParameters;
import org.thymeleaf.resourceresolver.IResourceResolver;

import java.io.InputStream;

public class ThymeleafResourceResolver implements IResourceResolver {

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public InputStream getResourceAsStream(
            TemplateProcessingParameters templateProcessingParameters,
            String resourceName) {
        return this.getClass().getResourceAsStream(resourceName);
    }

}
