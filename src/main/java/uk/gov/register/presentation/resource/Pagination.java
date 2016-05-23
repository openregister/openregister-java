package uk.gov.register.presentation.resource;


import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import java.util.Optional;

public class Pagination implements IPagination {
    public static final int ENTRY_LIMIT = 100;
    public static final String INDEX_PARAM = "page-index";
    public static final String SIZE_PARAM = "page-size";

    private final int pageIndex;
    private final int totalEntries;
    private final int pageSize;

    Pagination(Optional<Integer> optionalPageIndex, Optional<Integer> optionalPageSize, int totalEntries) {
        this.totalEntries = totalEntries;

        this.pageIndex = optionalPageIndex.orElse(1);
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

    int offset() {
        return (pageIndex - 1) * pageSize;
    }

    @Override
    public int getTotalEntries() {
        return totalEntries;
    }

    @Override
    public boolean isSinglePage() {
        return pageIndex == 1 && pageSize >= totalEntries;
    }

    @Override
    public int getFirstEntryNumberOnThisPage() {
        return offset() + 1;
    }

    @Override
    public int getLastEntryNumberOnThisPage() {
        int lastEntryNumber = pageIndex * pageSize;
        return lastEntryNumber > totalEntries ? totalEntries : lastEntryNumber;
    }

    @Override
    public boolean hasNextPage() {
        return totalEntries - (pageIndex * pageSize()) > 0;
    }

    @Override
    public boolean hasPreviousPage() {
        return pageIndex > 1;
    }

    @Override
    public int getNextPageNumber() {
        return pageIndex + 1;
    }

    @Override
    public int getPreviousPageNumber() {
        return pageIndex - 1;
    }

    @Override
    public String getNextPageLink() {
        return String.format("?" + INDEX_PARAM + "=%s&" + SIZE_PARAM + "=%s", getNextPageNumber(), pageSize());
    }

    @Override
    public String getPreviousPageLink() {
        return String.format("?" + INDEX_PARAM + "=%s&" + SIZE_PARAM + "=%s", getPreviousPageNumber(), pageSize());
    }

    @Override
    public int pageSize() {
        return pageSize;
    }

    @Override
    public int getTotalPages() {
        return (int) Math.round(Math.ceil(((double) totalEntries) / pageSize()));

    }
}
