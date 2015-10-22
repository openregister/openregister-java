package uk.gov.register.thymeleaf;

import org.markdownj.MarkdownProcessor;
import uk.gov.register.presentation.config.PublicBodiesConfiguration;
import uk.gov.register.presentation.config.PublicBody;
import uk.gov.register.presentation.resource.RequestContext;

public class HomePageView extends ThymeleafView {
    private final PublicBodiesConfiguration publicBodiesConfiguration;
    private final MarkdownProcessor markdownProcessor = new MarkdownProcessor();

    public HomePageView(PublicBodiesConfiguration publicBodiesConfiguration, RequestContext requestContext) {
        super(requestContext, "home.html");
        this.publicBodiesConfiguration = publicBodiesConfiguration;
    }

    @SuppressWarnings("unused, used from template")
    public PublicBody getPublicBody() {
        return publicBodiesConfiguration.getPublicBody(getRegister().getRegistry());
    }

    @SuppressWarnings("unused, used from template")
    public String getRegisterText() {
        return markdownProcessor.markdown(getRegister().getText());
    }
}
