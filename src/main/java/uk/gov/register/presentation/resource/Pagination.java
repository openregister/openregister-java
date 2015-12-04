package uk.gov.register.presentation.resource;


import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import java.util.Optional;

public class Pagination {
    public static final long ENTRY_LIMIT = 100;
    public static final String INDEX_PARAM = "page-index";
    public static final String SIZE_PARAM = "page-size";

    private final long pageIndex;
    private final String resourcePath;
    private final long totalEntries;
    private final long pageSize;

    Pagination(String resourcePath, Optional<Long> optionalPageIndex, Optional<Long> optionalPageSize, long totalEntries) {
        this.resourcePath = resourcePath;
        this.totalEntries = totalEntries;

        this.pageIndex = optionalPageIndex.orElse(1L);
        this.pageSize = optionalPageSize.orElse(ENTRY_LIMIT);

        if (this.pageSize <= 0) {
            throw new BadRequestException("page-size must be greater than 0.");
        }

        if (this.pageIndex <= 0) {
            throw new BadRequestException("page-index must be greater than 0.");
        }

        if (this.pageSize > 5000) {
            throw new BadRequestException("page-size too big, maximum page size can be 5000.");
        }

        if (pageIndex > 1 && ((pageIndex - 1) * pageSize) >= totalEntries) {
            throw new NotFoundException();
        }
    }

    long offset() {
        return (pageIndex - 1) * pageSize;
    }

    public long getTotalEntries() {
        return totalEntries;
    }

    public boolean isFirstPageAndShowsAllEntries() {
        return pageIndex == 1 && pageSize >= totalEntries;
    }

    public long getFirstEntryNumberOnThisPage() {
        return offset() + 1;
    }

    public long getLastEntryNumberOnThisPage() {
        long lastEntryNumber = pageIndex * pageSize;
        return lastEntryNumber > totalEntries ? totalEntries : lastEntryNumber;
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
        return String.format("%s?" + INDEX_PARAM + "=%s&" + SIZE_PARAM + "=%s", resourcePath, getNextPageNumber(), pageSize());
    }

    public String getPreviousPageLink() {
        return String.format("%s?" + INDEX_PARAM + "=%s&" + SIZE_PARAM + "=%s", resourcePath, getPreviousPageNumber(), pageSize());
    }

    public long pageSize() {
        return pageSize;
    }

    public long getTotalPages() {
        return Math.round(Math.ceil(((double) totalEntries) / pageSize()));

    }
}
