package uk.gov.register.views;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mock;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.resources.RequestContext;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(Parameterized.class)
public class DownloadPageViewTest {

    @Mock
    private RequestContext mockRequestContext;
    @Mock
    private RegisterReadOnly registerReadOnly;
    private final Boolean enableResourceDownload;

    public DownloadPageViewTest(final Boolean enableResourceDownload) {
        this.enableResourceDownload = enableResourceDownload;
    }

    @Parameters
    public static Collection<Boolean> data() {
        return Arrays.asList(true, false);
    }

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void getDownloadEnabled_returnsTheSameValuePassedInConstructor() throws Exception {
        final DownloadPageView downloadPageView;

        when(registerReadOnly.getTotalEntries()).thenReturn(1);
        when(registerReadOnly.getTotalRecords()).thenReturn(1);

        downloadPageView = new DownloadPageView(mockRequestContext, registerReadOnly, enableResourceDownload,
                register -> URI.create("http://" + register + ".test.register.gov.uk"));

        assertThat(downloadPageView.getDownloadEnabled(), equalTo(enableResourceDownload));
    }

}
