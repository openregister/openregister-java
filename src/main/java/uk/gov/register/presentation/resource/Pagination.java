package uk.gov.register.presentation.resource;


import javax.ws.rs.BadRequestException;
import java.util.Optional;

public class Pagination {

    private final long pageIndex;
    private final String resourcePath;
    private final long totalEntries;
    private final long pageSize;

    Pagination(String resourcePath, Optional<Long> pageIndex, Optional<Long> pageSize, long totalEntries) {
        this.resourcePath = resourcePath;
        this.totalEntries = totalEntries;

        this.pageIndex = pageIndex.orElse(1L);
        this.pageSize = pageSize.orElse(DataResource.ENTRY_LIMIT);

        if (this.pageSize <= 0 || this.pageIndex <= 0) {
            throw new BadRequestException();
        }
    }

    long offset() {
        return (pageIndex - 1) * pageSize;
    }

    public boolean hasNextPage() {
        return totalEntries - (pageIndex * pageSize()) > 0;
    }

    public boolean hasPreviousPage() {
        return pageIndex > 1;
    }

    public long getNextPageNumber() {
        return pageIndex + 1;
    }

    public long getPreviousPageNumber() {
        return pageIndex - 1;
    }

    public String getNextPageLink() {
        return String.format("%s?pageIndex=%s&pageSize=%s", resourcePath, getNextPageNumber(), pageSize());
    }

    public String getPreviousPageLink() {
        return String.format("%s?pageIndex=%s&pageSize=%s", resourcePath, getPreviousPageNumber(), pageSize());
    }

    public long pageSize() {
        return pageSize;
    }

    public long getTotalPages() {
        return Math.round(Math.ceil(((double) totalEntries) / pageSize()));

    }
}
