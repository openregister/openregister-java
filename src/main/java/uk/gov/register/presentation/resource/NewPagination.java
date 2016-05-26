package uk.gov.register.presentation.resource;

import java.util.Optional;

public class NewPagination implements IPagination {
    public final int start;
    public final int limit;

    private int totalEntries;

    public NewPagination(Optional<Integer> optionalStart, Optional<Integer> optionalLimit, int totalEntries) {
        this.start = optionalStart.orElseGet(() -> 1);
        this.limit = optionalLimit.orElseGet(() -> 100);
        this.totalEntries = totalEntries;
    }

    @Override
    public int getTotalEntries() {
        return totalEntries;
    }

    @Override
    public boolean hasNextPage() {
        return start + limit - 1 < totalEntries;
    }

    @Override
    public boolean hasPreviousPage() {
        return start > 1;
    }

    @Override
    public String getPreviousPageLink() {
        return String.format("?start=%s&limit=%s", start - limit, limit);
    }

    @Override
    public String getNextPageLink() {
        return String.format("?start=%s&limit=%s", start + limit, limit);
    }

    @Override
    public int getTotalPages() {
        if (start < 1) {
            int actualStart = (start - 1) % limit;
            int totalPages = (int) Math.ceil((double) (totalEntries - actualStart + 1) / limit);
            return totalPages + (actualStart / limit);
        } else {
            if ((start - 1) % limit != 0)
                return (totalEntries / limit) + 1;
            return (int) Math.ceil((double) totalEntries / limit);
        }
    }

    @Override
    public int getFirstEntryNumberOnThisPage() {
        return start > 0 ? start : 1;
    }

    @Override
    public int getLastEntryNumberOnThisPage() {
        int lastEntryNumber = start + limit - 1;
        return lastEntryNumber > totalEntries ? totalEntries : lastEntryNumber;
    }

    @Override
    public int getPreviousPageNumber() {
        return (int) Math.ceil(((double) start - 1) / limit);
    }

    @Override
    public int getNextPageNumber() {
        return getPreviousPageNumber() + 2;
    }

    @Override
    public boolean isSinglePage() {
        if (start > 1)
            return false;
        else
            return start + limit - 1 >= totalEntries;
    }
}
