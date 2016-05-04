package uk.gov.register.presentation.resource;

import java.util.Optional;

public class NewPagination {
    public final int start;
    public final int limit;

    private int totalEntries;

    public NewPagination(Optional<Integer> optionalStart, Optional<Integer> optionalLimit, int totalEntries) {
        this.start = optionalStart.orElseGet(() -> 1);
        this.limit = optionalLimit.orElseGet(() -> 100);
        this.totalEntries = totalEntries;
    }

    @SuppressWarnings("unused, used by html")
    public int getTotalEntries() {
        return totalEntries;
    }

    public boolean hasNextPage() {
        return start + limit - 1 < totalEntries;
    }

    public boolean hasPreviousPage() {
        return start > 1;
    }

    public String getPreviousPageLink() {
        return String.format("?start=%s&limit=%s", start - limit, limit);
    }

    public String getNextPageLink() {
        return String.format("?start=%s&limit=%s", start + limit, limit);
    }

    public int getTotalPages() {
        if (start < 1) {
            int actualStart = (start - 1) % limit;
            int totalPages = (int) Math.ceil((double) (totalEntries - actualStart + 1) / limit);
            return totalPages + (actualStart / limit);
        } else {
            if ((start - 1) % limit != 0)
                return (totalEntries / limit) + 1;
            return totalEntries / limit;
        }
    }

    public int getFirstEntryNumberOnThisPage() {
        return start > 0 ? start : 1;
    }

    public int getLastEntryNumberOnThisPage() {
        int lastEntryNumber = start + limit - 1;
        return lastEntryNumber > totalEntries ? totalEntries : lastEntryNumber;
    }

    public int getPreviousPageNumber() {
        return (int) Math.ceil(((double) start - 1) / limit);
    }

    public int getNextPageNumber() {
        return getPreviousPageNumber() + 2;
    }

    public boolean isSinglePage() {
        if (start > 1)
            return false;
        else
            return start + limit - 1 >= totalEntries;
    }
}
