package uk.gov.register.views;

import uk.gov.register.configuration.HomepageContent;
import uk.gov.register.core.RegisterId;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.core.RegisterResolver;
import uk.gov.register.resources.RequestContext;
import uk.gov.register.util.ElementType;

import javax.inject.Provider;

public class PreviewRecordPageView extends PreviewPageView {

    private final RecordsView recordsView;

    public PreviewRecordPageView(final RequestContext requestContext,
                                 final RegisterReadOnly register,
                                 final RegisterResolver registerResolver,
                                 final String previewType,
                                 final HomepageContent homepageContent,
                                 final RecordsView recordsView,
                                 final Provider<RegisterId> registerIdProvider,
                                 final String key) {
        super(requestContext, registerResolver, register, key, previewType,
                ElementType.RECORD, homepageContent, registerIdProvider);
        this.recordsView = recordsView;
    }

    @Override
    public String getRegisterValues() {
        return recordsView.recordsTo(getPreviewType(), getRegisterIdProvider(), getRegisterResolver());
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
