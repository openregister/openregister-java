package thymeleaf;

import org.thymeleaf.templateresolver.TemplateResolver;

public class ThymeleafResourceTemplateResolver extends TemplateResolver {

    public ThymeleafResourceTemplateResolver() {
        super.setResourceResolver(new ThymeleafResourceResolver());
    }


//    @Override
//    public void setResourceResolver(IResourceResolver resourceResolver) {
//        throw new ConfigurationException(
//                "Cannot set a resource resolver on " + this.getClass().getName() + ". If " +
//                        "you want to set your own resource resolver, use " + TemplateResolver.class.getName() +
//                        "instead");
//    }


}
