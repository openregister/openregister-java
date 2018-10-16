package uk.gov.register.views;

import uk.gov.register.configuration.HomepageContent;
import uk.gov.register.core.RegisterId;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.core.RegisterResolver;
import uk.gov.register.resources.RequestContext;
import uk.gov.register.util.ElementType;

import javax.inject.Provider;

public class PreviewBlobPageView extends PreviewPageView {

    private final BlobView blobView;

    public PreviewBlobPageView(final RequestContext requestContext,
                               final RegisterReadOnly register,
                               final RegisterResolver registerResolver,
                               final String previewType,
                               final HomepageContent homepageContent,
                               final BlobView blobView,
                               final Provider<RegisterId> registerIdProvider,
                               final String key) {
        super(requestContext, registerResolver, register, key, previewType,
                ElementType.ITEM, homepageContent, registerIdProvider);
        this.blobView = blobView;
    }

    @Override
    public String getRegisterValues() {
        return blobView.itemsTo(getPreviewType(), getRegisterIdProvider(), getRegisterResolver());
    }

    @Override
    public int getTotalObjects() {
        return 1;
    }

    @Override
    public boolean getIsSingleKey() {
        return true;
    }
}
