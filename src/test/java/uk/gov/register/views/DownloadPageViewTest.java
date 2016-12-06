package uk.gov.register.views;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mock;
import uk.gov.register.core.EmptyRegister;
import uk.gov.register.resources.RequestContext;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class DownloadPageViewTest {

    @Mock
    private RequestContext mockRequestContext;
    private Boolean enableResourceDownload;

    public DownloadPageViewTest(Boolean enableResourceDownload) {
        this.enableResourceDownload = enableResourceDownload;
    }

    @Parameters
    public static Collection<Boolean> data() {
        return Arrays.asList(true, false);
    }

    @Test
    public void getDownloadEnabled_returnsTheSameValuePassedInConstructor() throws Exception {
        DownloadPageView downloadPageView = new DownloadPageView(mockRequestContext, enableResourceDownload, () -> Optional.empty(), register -> URI.create("http://" + register + ".test.register.gov.uk"), new EmptyRegister());
        assertThat(downloadPageView.getDownloadEnabled(), equalTo(enableResourceDownload));
    }
}
