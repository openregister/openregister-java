package uk.gov.register.views;

import uk.gov.register.configuration.HomepageContent;
import uk.gov.register.configuration.RegisterTrackingConfiguration;
import uk.gov.register.core.RegisterName;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.core.RegisterResolver;
import uk.gov.register.resources.RequestContext;
import uk.gov.register.util.ElementType;

import javax.inject.Provider;

public class PreviewEntryPageView extends PreviewPageView {

    private final EntryListView entriesView;

    public PreviewEntryPageView(final RequestContext requestContext,
                                final RegisterReadOnly register,
                                final RegisterTrackingConfiguration registerTrackingConfiguration,
                                final RegisterResolver registerResolver,
                                final String previewType,
                                final HomepageContent homepageContent,
                                final EntryListView entriesView,
                                final Provider<RegisterName> registerNameProvider,
                                final Integer key) {
        super(requestContext, registerTrackingConfiguration, registerResolver, register, String.valueOf(key), previewType,
                ElementType.ENTRY, homepageContent, registerNameProvider);
        this.entriesView = entriesView;
    }

    @Override
    public String getRegisterValues() {
        return entriesView.entriesTo(getPreviewType(), getRegisterNameProvider(), getRegisterResolver());
    }

    @Override
    public int getTotalObjects() {
        return getTotalEntries();
    }

    @Override
    public boolean getIsSingleKey() {
        return entriesView.getEntries().size() == 1;
    }
}
