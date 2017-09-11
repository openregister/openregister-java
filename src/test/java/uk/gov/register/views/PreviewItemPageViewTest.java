package uk.gov.register.views;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
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
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class PreviewItemPageViewTest {

    private final RegisterResolver registerResolver = registerName -> URI.create("http://" + registerName + ".test.register.gov.uk");

    private final Iterable<Field> fields = ImmutableList.of(
            new Field("key1", "datatype", new RegisterName("address"), Cardinality.ONE, "text"));

    @Mock
    private RequestContext requestContext;

    @Mock
    private RegisterReadOnly registerReadOnly;

    private final HomepageContent homepageContent = new HomepageContent(Optional.empty(), emptyList(), emptyList());
    private final Provider<RegisterName> provider = mock(Provider.class);

    @Test
    public void getPreviewType_JSON() throws IOException, JSONException {
        final PreviewItemPageView previewItemPageView = new PreviewItemPageView(
                requestContext,
                registerReadOnly,
                Optional::empty,
                registerResolver,
                "json",
                homepageContent,
                getItemView(),
                provider,
                null);

        assertThat(previewItemPageView.getPreviewTypeTag(), containsString("JSON"));
    }

    @Test
    public void getPreviewType_YAML() throws IOException, JSONException {
        final PreviewItemPageView previewItemPageView = new PreviewItemPageView(
                requestContext,
                registerReadOnly,
                Optional::empty,
                registerResolver,
                "yaml",
                homepageContent,
                getItemView(),
                provider,
                null);

        assertThat(previewItemPageView.getPreviewTypeTag(), containsString("YAML"));
    }

    @Test
    public void getPreviewType_TTL() throws IOException, JSONException {
        final PreviewItemPageView previewItemPageView = new PreviewItemPageView(
                requestContext,
                registerReadOnly,
                Optional::empty,
                registerResolver,
                "turtle",
                homepageContent,
                getItemView(),
                provider,
                null);

        assertThat(previewItemPageView.getPreviewTypeTag(), containsString("TTL"));
    }

    @Test
    public void getRegisterValues_JSON() throws IOException, JSONException {
        final PreviewItemPageView previewItemPageView = new PreviewItemPageView(
                requestContext,
                registerReadOnly,
                Optional::empty,
                registerResolver,
                "json",
                homepageContent,
                getItemView(),
                provider,
                null);

        assertThat(previewItemPageView.getRegisterValues(), containsString("value1"));
    }

    @Test
    public void getRegisterValues_YAML() throws IOException, JSONException {
        final PreviewItemPageView previewItemPageView = new PreviewItemPageView(
                requestContext,
                registerReadOnly,
                Optional::empty,
                registerResolver,
                "yaml",
                homepageContent,
                getItemView(),
                provider,
                null);

        assertThat(previewItemPageView.getRegisterValues(), containsString("value1"));
    }

    @Test
    public void getRegisterValues_TTL() throws IOException, JSONException {
        final PreviewItemPageView previewItemPageView = new PreviewItemPageView(
                requestContext,
                registerReadOnly,
                Optional::empty,
                registerResolver,
                "turtle",
                homepageContent,
                getItemView(),
                provider,
                null);

        assertThat(previewItemPageView.getRegisterValues(), containsString("value1"));
    }

    private ItemView getItemView() throws IOException, JSONException {
        final ImmutableMap<String, FieldValue> fieldValueMap = ImmutableMap.of(
                "key1", new StringValue("value1")
        );

        return new ItemView(new HashValue(HashingAlgorithm.SHA256, "asdfghhjkl"), fieldValueMap, fields);
    }
}
