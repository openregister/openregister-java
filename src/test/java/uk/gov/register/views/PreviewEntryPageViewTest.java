package uk.gov.register.views;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.register.configuration.HomepageContent;
import uk.gov.register.core.*;
import uk.gov.register.resources.RequestContext;
import uk.gov.register.util.HashValue;

import javax.inject.Provider;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class PreviewEntryPageViewTest {

    private final RegisterResolver registerResolver = registerName -> URI.create("http://" + registerName + ".test.register.gov.uk");

    @Mock
    private RequestContext requestContext;

    @Mock
    private RegisterReadOnly registerReadOnly;

    private final HomepageContent homepageContent = new HomepageContent(Optional.empty(), emptyList(), emptyList());
    private final Provider<RegisterName> provider = mock(Provider.class);

    @Test
    public void getPreviewType_JSON() throws IOException, JSONException {
        final PreviewEntryPageView previewEntryPageView = new PreviewEntryPageView(
                requestContext,
                registerReadOnly,
                registerResolver,
                "json",
                homepageContent,
                getEntriesView(),
                provider,
                null);

        assertThat(previewEntryPageView.getPreviewTypeTag(), containsString("JSON"));
    }

    @Test
    public void getPreviewType_YAML() throws IOException, JSONException {
        final PreviewEntryPageView previewEntryPageView = new PreviewEntryPageView(
                requestContext,
                registerReadOnly,
                registerResolver,
                "yaml",
                homepageContent,
                getEntriesView(),
                provider,
                null);

        assertThat(previewEntryPageView.getPreviewTypeTag(), containsString("YAML"));
    }

    @Test
    public void getPreviewType_TTL() throws IOException, JSONException {
        final PreviewEntryPageView previewEntryPageView = new PreviewEntryPageView(
                requestContext,
                registerReadOnly,
                registerResolver,
                "turtle",
                homepageContent,
                getEntriesView(),
                provider,
                null);

        assertThat(previewEntryPageView.getPreviewTypeTag(), containsString("TTL"));
    }

    @Test
    public void getRegisterValues_JSON() throws IOException, JSONException {
        final PreviewEntryPageView previewEntryPageView = new PreviewEntryPageView(
                requestContext,
                registerReadOnly,
                registerResolver,
                "json",
                homepageContent,
                getEntriesView(),
                provider,
                null);

        assertThat(previewEntryPageView.getRegisterValues(), containsString("asdfghhjkl"));
    }

    @Test
    public void getRegisterValues_YAML() throws IOException, JSONException {
        final PreviewEntryPageView previewEntryPageView = new PreviewEntryPageView(
                requestContext,
                registerReadOnly,
                registerResolver,
                "yaml",
                homepageContent,
                getEntriesView(),
                provider,
                null);

        assertThat(previewEntryPageView.getRegisterValues(), containsString("asdfghhjkl"));
    }

    @Test
    public void getRegisterValues_TTL() throws IOException, JSONException {
        final PreviewEntryPageView previewEntryPageView = new PreviewEntryPageView(
                requestContext,
                registerReadOnly,
                registerResolver,
                "turtle",
                homepageContent,
                getEntriesView(),
                provider,
                null);

        assertThat(previewEntryPageView.getRegisterValues(), containsString("asdfghhjkl"));
    }

    private EntryListView getEntriesView() throws IOException, JSONException {
        final Entry entry = new Entry(1,
                new HashValue(HashingAlgorithm.SHA256, "asdfghhjkl"),
                Instant.now(),
                "KEY",
                EntryType.user);

        return new EntryListView(Collections.singletonList(entry));
    }
}
