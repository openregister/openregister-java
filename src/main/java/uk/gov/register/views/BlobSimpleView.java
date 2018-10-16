package uk.gov.register.views;

public class BlobSimpleView extends BlobView {

    private final String key;

    public BlobSimpleView(final String key, final BlobView blobView) {
        super(blobView.getBlobHash(), blobView.getContent(), blobView.getFields());
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
