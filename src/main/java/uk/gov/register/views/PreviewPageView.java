package uk.gov.register.views;

import org.apache.commons.lang3.StringUtils;
import uk.gov.register.configuration.HomepageContent;
import uk.gov.register.core.RegisterName;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.core.RegisterResolver;
import uk.gov.register.resources.RequestContext;
import uk.gov.register.thymeleaf.ThymeleafView;
import uk.gov.register.util.ElementType;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.inject.Provider;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public abstract class PreviewPageView extends ThymeleafView {

    private static final String NUMBER_FORMAT = "#,###";
    private static final String PREVIEW_HTML_PAGE = "preview.html";

    protected static final String YAML = "YAML";
    protected static final String TTL = "TTL";
    protected static final String JSON = "JSON";

    private final String key;
    private final String previewType;
    private final HomepageContent homepageContent;
    private final Provider<RegisterName> registerNameProvider;
    private final ElementType elementType;
    private final int totalRecords;
    private final int totalEntries;

    public PreviewPageView(final RequestContext requestContext,
                           final RegisterResolver registerResolver,
                           final RegisterReadOnly register,
                           final String key,
                           final String previewType,
                           final ElementType elementType,
                           final HomepageContent homepageContent,
                           final Provider<RegisterName> registerNameProvider) {
        super(requestContext, PREVIEW_HTML_PAGE, registerResolver, register);
        this.key = key;
        this.previewType = previewType;
        this.elementType = elementType;
        this.homepageContent = homepageContent;
        this.registerNameProvider = registerNameProvider;
        totalRecords = register.getTotalRecords();
        totalEntries = register.getTotalEntries();
    }

    public abstract String getRegisterValues();

    @SuppressWarnings("unused, used by templates")
    public String getTotalElements() {
        final NumberFormat nf = new DecimalFormat(NUMBER_FORMAT);

        return nf.format(getTotalObjects());
    }

    public String getPreviewTypeTag() {
        if (ExtraMediaType.TEXT_YAML_TYPE.getSubtype().equals(previewType)) {
            return YAML;
        } else if (ExtraMediaType.TEXT_TTL_TYPE.getSubtype().equals(previewType)) {
            return TTL;
        } else {
            return JSON;
        }
    }

    @SuppressWarnings("unused, used by templates")
    public String getUrlMultipleElements() {
        if (elementType.getMultipleElementsDownloadLocation().isEmpty()) {
            return StringUtils.EMPTY;
        }

        return String.format(elementType.getMultipleElementsDownloadLocation(), getPreviewTypeTag().toLowerCase());
    }

    @SuppressWarnings("unused, used by templates")
    public String getUrlSingleElements() {
        return String.format(elementType.getSingleElementDownloadLocation(), getKey(), getPreviewTypeTag().toLowerCase());
    }

    public abstract int getTotalObjects();

    @SuppressWarnings("unused, used by templates")
    public abstract boolean getIsSingleKey();

    @SuppressWarnings("unused, used from template")
    public int getTotalRecords() {
        return totalRecords;
    }

    @SuppressWarnings("unused, used from template")
    public int getTotalEntries() {
        return totalEntries;
    }

    public String getKey() {
        return key;
    }

    public String getPreviewType() {
        return previewType;
    }

    public HomepageContent getHomepageContent() {
        return homepageContent;
    }

    public Provider<RegisterName> getRegisterNameProvider() {
        return registerNameProvider;
    }
}
