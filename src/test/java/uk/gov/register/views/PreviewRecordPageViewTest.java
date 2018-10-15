package uk.gov.register.views;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.jackson.Jackson;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.register.configuration.HomepageContent;
import uk.gov.register.core.*;
import uk.gov.register.resources.RequestContext;
import uk.gov.register.service.ItemConverter;
import uk.gov.register.util.HashValue;

import javax.inject.Provider;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PreviewRecordPageViewTest {

    private final RegisterResolver registerResolver = registerId -> URI.create("http://" + registerId + ".test.register.gov.uk");

    @Mock
    private RequestContext requestContext;

    @Mock
    private RegisterReadOnly registerReadOnly;

    private final HomepageContent homepageContent = new HomepageContent(emptyList());
    private final Provider<RegisterId> provider = mock(Provider.class);

    @Test
    public void getPreviewType_JSON() throws IOException, JSONException {
        final PreviewRecordPageView previewRecordPageView = new PreviewRecordPageView(
                requestContext,
                registerReadOnly,
                registerResolver,
                "json",
                homepageContent,
                getRecordsView(),
                provider,
                null);

        assertThat(previewRecordPageView.getPreviewTypeTag(), containsString("JSON"));
    }

    @Test
    public void getPreviewType_YAML() throws IOException, JSONException {
        final PreviewRecordPageView previewRecordPageView = new PreviewRecordPageView(
                requestContext,
                registerReadOnly,
                registerResolver,
                "yaml",
                homepageContent,
                getRecordsView(),
                provider,
                null);

        assertThat(previewRecordPageView.getPreviewTypeTag(), containsString("YAML"));
    }

    @Test
    public void getPreviewType_TTL() throws IOException, JSONException {
        final PreviewRecordPageView previewRecordPageView = new PreviewRecordPageView(
                requestContext,
                registerReadOnly,
                registerResolver,
                "turtle",
                homepageContent,
                getRecordsView(),
                provider,
                null);

        assertThat(previewRecordPageView.getPreviewTypeTag(), containsString("TTL"));
    }

    @Test
    public void getRegisterValues_JSON() throws IOException, JSONException {
        final PreviewRecordPageView previewRecordPageView = new PreviewRecordPageView(
                requestContext,
                registerReadOnly,
                registerResolver,
                "json",
                homepageContent,
                getRecordsView(),
                provider,
                null);

        assertThat(previewRecordPageView.getRegisterValues(), containsString("123"));
    }

    @Test
    public void getRegisterValues_YAML() throws IOException, JSONException {
        final PreviewRecordPageView previewRecordPageView = new PreviewRecordPageView(
                requestContext,
                registerReadOnly,
                registerResolver,
                "yaml",
                homepageContent,
                getRecordsView(),
                provider,
                null);

        assertThat(previewRecordPageView.getRegisterValues(), containsString("123"));
    }

    @Test
    public void getRegisterValues_TTL() throws IOException, JSONException {
        final PreviewRecordPageView previewRecordPageView = new PreviewRecordPageView(
                requestContext,
                registerReadOnly,
                registerResolver,
                "turtle",
                homepageContent,
                getRecordsView(),
                provider,
                null);

        assertThat(previewRecordPageView.getRegisterValues(), containsString("123"));
    }

    private RecordsView getRecordsView() throws IOException, JSONException {
        final ObjectMapper objectMapper = Jackson.newObjectMapper();
        final Instant t1 = Instant.parse("2016-03-29T08:59:25Z");
        final Instant t2 = Instant.parse("2016-03-28T09:49:26Z");
        final Entry entry1 = new Entry(1, new HashValue(HashingAlgorithm.SHA256, "ab"), t1, "123", EntryType.user);
        final Entry entry2 = new Entry(2, new HashValue(HashingAlgorithm.SHA256, "cd"), t2, "456", EntryType.user);
        final Blob blob1 = new Blob(new HashValue(HashingAlgorithm.SHA256, "ab"), objectMapper.readTree("{\"address\":\"123\",\"street\":\"foo\"}"));
        final Blob blob2 = new Blob(new HashValue(HashingAlgorithm.SHA256, "cd"), objectMapper.readTree("{\"address\":\"456\",\"street\":\"bar\"}"));
        final Record record1 = new Record(entry1, Collections.singletonList(blob1));
        final Record record2 = new Record(entry2, Collections.singletonList(blob2));
        final ItemConverter itemConverter = mock(ItemConverter.class);
        final Map<String, Field> fieldsByName = mock(Map.class);

        when(itemConverter.convertItem(blob1, fieldsByName)).thenReturn(ImmutableMap.of("address", new StringValue("123"),
                "street", new StringValue("foo")));
        when(itemConverter.convertItem(blob2, fieldsByName)).thenReturn(ImmutableMap.of("address", new StringValue("456"),
                "street", new StringValue("bar")));

        return new RecordsView(Arrays.asList(record1, record2), fieldsByName, itemConverter, false, false);
    }
}
