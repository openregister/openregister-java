package uk.gov.register.presentation.resource;


import javax.ws.rs.BadRequestException;
import java.util.Optional;

class Pagination {

    private final long pageIndex;
    private final long totalEntries;
    private final long pageSize;

    Pagination(Optional<Long> pageIndex, Optional<Long> pageSize, long totalEntries) {
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

    public long nextPageNumber() {
        return pageIndex + 1;
    }

    public long previousPageNumber() {
        return pageIndex - 1;
    }

    public long pageSize() {
        return pageSize;
    }
}
