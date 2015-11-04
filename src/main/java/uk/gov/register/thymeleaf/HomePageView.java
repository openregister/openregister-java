package uk.gov.register.thymeleaf;

import org.markdownj.MarkdownProcessor;
import uk.gov.register.presentation.config.PublicBodiesConfiguration;
import uk.gov.register.presentation.config.PublicBody;
import uk.gov.register.presentation.dao.RecentEntryIndexQueryDAO;
import uk.gov.register.presentation.resource.RequestContext;

public class HomePageView extends ThymeleafView {
    private final PublicBodiesConfiguration publicBodiesConfiguration;
    private final RecentEntryIndexQueryDAO recentEntryIndexQueryDAO;
    private final MarkdownProcessor markdownProcessor = new MarkdownProcessor();

    public HomePageView(PublicBodiesConfiguration publicBodiesConfiguration, RequestContext requestContext, RecentEntryIndexQueryDAO recentEntryIndexQueryDAO) {
        super(requestContext, "home.html");
        this.publicBodiesConfiguration = publicBodiesConfiguration;
        this.recentEntryIndexQueryDAO = recentEntryIndexQueryDAO;
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
        return recentEntryIndexQueryDAO.getTotalRecords();
    }

}
