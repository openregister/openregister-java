package uk.gov.register.thymeleaf;

import org.markdownj.MarkdownProcessor;
import uk.gov.register.presentation.config.PublicBodiesConfiguration;
import uk.gov.register.presentation.config.PublicBody;
import uk.gov.register.presentation.resource.RequestContext;

public class HomePageView extends ThymeleafView {
    private final PublicBodiesConfiguration publicBodiesConfiguration;
    private final int totalRecords;
    private final MarkdownProcessor markdownProcessor = new MarkdownProcessor();

    public HomePageView(PublicBodiesConfiguration publicBodiesConfiguration, RequestContext requestContext, int totalRecords) {
        super(requestContext, "home.html");
        this.publicBodiesConfiguration = publicBodiesConfiguration;
        this.totalRecords = totalRecords;
    }

    @SuppressWarnings("unused, used from template")
    public PublicBody getPublicBody() {
        return publicBodiesConfiguration.getPublicBody(getRegister().getRegistry());
    }

    @SuppressWarnings("unused, used from template")
    public String getRegisterText() {
        return markdownProcessor.markdown(getRegister().getText());
    }

    @SuppressWarnings("unused, used from template")
    public int getTotalRecords(){
        return totalRecords;
    }
}
