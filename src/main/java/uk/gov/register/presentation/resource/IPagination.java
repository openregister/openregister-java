package uk.gov.register.presentation.resource;

public interface IPagination {
    long getTotalEntries();

    boolean isSinglePage();

    long getFirstEntryNumberOnThisPage();

    long getLastEntryNumberOnThisPage();

    boolean hasNextPage();

    boolean hasPreviousPage();

    long getNextPageNumber();

    long getPreviousPageNumber();

    String getNextPageLink();

    String getPreviousPageLink();

    long pageSize();

    long getTotalPages();
}
