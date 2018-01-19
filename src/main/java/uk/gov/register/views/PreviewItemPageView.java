package uk.gov.register.views;

import uk.gov.register.configuration.HomepageContent;
import uk.gov.register.core.RegisterName;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.core.RegisterResolver;
import uk.gov.register.resources.RequestContext;
import uk.gov.register.util.ElementType;

import javax.inject.Provider;

public class PreviewItemPageView extends PreviewPageView {

    private final ItemView itemView;

    public PreviewItemPageView(final RequestContext requestContext,
                               final RegisterReadOnly register,
                               final RegisterResolver registerResolver,
                               final String previewType,
                               final HomepageContent homepageContent,
                               final ItemView itemView,
                               final Provider<RegisterName> registerNameProvider,
                               final String key) {
        super(requestContext, registerResolver, register, key, previewType,
                ElementType.ITEM, homepageContent, registerNameProvider);
        this.itemView = itemView;
    }

    @Override
    public String getRegisterValues() {
        return itemView.itemsTo(getPreviewType(), getRegisterNameProvider(), getRegisterResolver());
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
