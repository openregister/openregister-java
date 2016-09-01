package uk.gov.register.functional;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Assert;
import org.junit.Test;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class AssetsBundleCustomErrorHandlerTest extends FunctionalTestBase {
    @Test
    public void displays404pageForNonexistentAssets() {
        Response response = getRequest("/assets/not-an-assets");

        assertThat(response.getStatus(), equalTo(404));
        assertThat(response.getMediaType().isCompatible(MediaType.TEXT_HTML_TYPE), equalTo(true));

        Document doc = Jsoup.parse(response.readEntity(String.class));
        Elements notFoundHeader = doc.select("main h1");
        Assert.assertThat(notFoundHeader.first().text(), equalTo("Page not found"));
    }
}
