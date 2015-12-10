package uk.gov.register.presentation.view;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.register.presentation.EntryView;
import uk.gov.register.presentation.StringValue;
import uk.gov.register.presentation.config.PublicBody;
import uk.gov.register.presentation.resource.RequestContext;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SingleEntryViewTest {
    @Mock
    private RequestContext requestContext;
    @Mock
    private PublicBody custodian;

    @Test
    public void getVersionHistoryLink_constructsCorrectUrl() throws Exception {
        when(requestContext.getRegisterPrimaryKey()).thenReturn("primaryKey");
        SingleEntryView singleEntryView = new SingleEntryView(requestContext, new EntryView(50, "hash", "primaryKey", ImmutableMap.of("primaryKey", new StringValue("12345"))), custodian);

        String versionHistoryLink = singleEntryView.getVersionHistoryLink();

        assertThat(versionHistoryLink, equalTo("/primaryKey/12345/history"));
    }
}
