package uk.gov.register.util;

public enum ElementType {
    RECORD("/records.%s", "/record/%s.%s"),
    ENTRY("/entries.%s", "/entry/%s.%s"),
    ITEM("", "/item/sha-256:%s.%s");

    private final String multipleElementsDownloadLocation;
    private final String singleElementDownloadLocation;

    ElementType(final String multipleElementsDownloadLocation, final String singleElementDownloadLocation) {
        this.multipleElementsDownloadLocation = multipleElementsDownloadLocation;
        this.singleElementDownloadLocation = singleElementDownloadLocation;
    }

    public String getMultipleElementsDownloadLocation() {
        return multipleElementsDownloadLocation;
    }

    public String getSingleElementDownloadLocation() {
        return singleElementDownloadLocation;
    }
}
