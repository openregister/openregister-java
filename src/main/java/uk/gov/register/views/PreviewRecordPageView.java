package uk.gov.register.views;

import uk.gov.register.configuration.HomepageContent;
import uk.gov.register.configuration.RegisterTrackingConfiguration;
import uk.gov.register.core.RegisterName;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.core.RegisterResolver;
import uk.gov.register.resources.RequestContext;
import uk.gov.register.util.ElementType;

import javax.inject.Provider;

public class PreviewRecordPageView extends PreviewPageView {

    private final RecordsView recordsView;

    public PreviewRecordPageView(final RequestContext requestContext,
                                 final RegisterReadOnly register,
                                 final RegisterTrackingConfiguration registerTrackingConfiguration,
                                 final RegisterResolver registerResolver,
                                 final String previewType,
                                 final HomepageContent homepageContent,
                                 final RecordsView recordsView,
                                 final Provider<RegisterName> registerNameProvider,
                                 final String key) {
        super(requestContext, registerTrackingConfiguration, registerResolver, register, key, previewType,
                ElementType.RECORD, homepageContent, registerNameProvider);
        this.recordsView = recordsView;
    }

    @Override
    public String getRegisterValues() {
        return recordsView.recordsTo(getPreviewType(), getRegisterNameProvider(), getRegisterResolver());
    }

    @Override
    public int getTotalObjects() {
        return getTotalRecords();
    }

    @Override
    public boolean getIsSingleKey() {
        return recordsView.getRecords().size() == 1;
    }
}
