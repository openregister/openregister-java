package uk.gov.register.presentation.resource;

public interface IPagination {
    int getTotalEntries();

    boolean isSinglePage();

    int getFirstEntryNumberOnThisPage();

    int getLastEntryNumberOnThisPage();

    boolean hasNextPage();

    boolean hasPreviousPage();

    int getNextPageNumber();

    int getPreviousPageNumber();

    String getNextPageLink();

    String getPreviousPageLink();

    int getTotalPages();
}
