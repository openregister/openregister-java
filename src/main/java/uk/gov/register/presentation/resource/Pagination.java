package uk.gov.register.presentation.resource;

class Pagination {

    private final long pageIndex;
    private final long totalEntries;
    private final long pageSize;

    Pagination(long pageIndex, long pageSize, long totalEntries) {
        this.totalEntries = totalEntries;
        this.pageIndex = pageIndex < 1 ? 1 : pageIndex;
        this.pageSize = pageSize < 1 ? DataResource.ENTRY_LIMIT : pageSize;
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
