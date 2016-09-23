package uk.gov.register.resources;

import org.junit.Test;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class IndexSizePaginationTest {

    @Test(expected = BadRequestException.class)
    public void construct_throwsExceptionForPageSizeGreaterThan5000() {
        new IndexSizePagination(Optional.of(1), Optional.of(5001), 10);
    }

    @Test(expected = BadRequestException.class)
    public void construct_throwsExceptionWhenPageSizeIsZero() {
        new IndexSizePagination(Optional.of(1), Optional.of(0), 10);
    }

    @Test(expected = BadRequestException.class)
    public void construct_throwsExceptionWhenPageSizeIsNegativeNumber() {
        new IndexSizePagination(Optional.of(1), Optional.of(-1), 10);
    }

    @Test(expected = BadRequestException.class)
    public void construct_throwsExceptionWhenPageIndexIsZero() {
        new IndexSizePagination(Optional.of(0), Optional.of(1), 10);
    }

    @Test(expected = BadRequestException.class)
    public void construct_throwsExceptionWhenPageIndexIsNegativeNumber() {
        new IndexSizePagination(Optional.of(-1), Optional.of(1), 10);
    }

    @Test(expected = NotFoundException.class)
    public void construct_throwsNotFoundException_whenNoMoreEntriesForGivenPageSizeAndPageIndexValues() {
        new IndexSizePagination(Optional.of(2), Optional.of(10), 10);
    }

    @Test
    public void offset_returnsTheNumberWhichOffsetsTheTotalEntriesBasedOnPageSize() {
        assertThat(new IndexSizePagination(Optional.of(1), Optional.of(10), 100).offset(), equalTo(0));
        assertThat(new IndexSizePagination(Optional.of(2), Optional.of(10), 100).offset(), equalTo(10));
    }

    @Test
    public void hasNextPage_returnsTrueOnlyWhenThereAreMoreEntriesAvailable() {
        assertFalse(new IndexSizePagination(Optional.of(1), Optional.of(10), 10).hasNextPage());
        assertFalse(new IndexSizePagination(Optional.of(2), Optional.of(10), 20).hasNextPage());

        assertTrue(new IndexSizePagination(Optional.of(1), Optional.of(10), 11).hasNextPage());

        assertTrue(new IndexSizePagination(Optional.of(2), Optional.of(10), 21).hasNextPage());
    }

    @Test
    public void hasPreviousPage_returnsTrueOnlyWhenPageIndexIsMoreThanOne() {
        assertFalse(new IndexSizePagination(Optional.of(1), Optional.of(10), 10).hasPreviousPage());
        assertFalse(new IndexSizePagination(Optional.of(1), Optional.of(10), 11).hasPreviousPage());

        assertTrue(new IndexSizePagination(Optional.of(2), Optional.of(10), 11).hasPreviousPage());
    }

    @Test
    public void getTotalPages_returnsTotalNumberOfPages() {
        IndexSizePagination pagination = new IndexSizePagination(
                Optional.of(1),
                Optional.of(10),
                10);
        assertThat(pagination.getTotalPages(), equalTo(1));


        pagination = new IndexSizePagination(
                Optional.of(1),
                Optional.of(10),
                11);
        assertThat(pagination.getTotalPages(), equalTo(2));

    }

    @Test
    public void getNextPageLink_returnsTheLinkForNextPage() {
        IndexSizePagination pagination = new IndexSizePagination(Optional.of(1), Optional.of(10), 11);
        assertThat(pagination.getNextPageLink(), equalTo("?page-index=2&page-size=10"));
    }

    @Test
    public void getPreviousPageLink_returnsTheLinkForPreviousPage() {
        IndexSizePagination pagination = new IndexSizePagination(Optional.of(2), Optional.of(10), 11);
        assertThat(pagination.getPreviousPageLink(), equalTo("?page-index=1&page-size=10"));
    }

    @Test
    public void getFirstEntryNumberOnThisPage_returnsTheNumberOfFirstEntryOnPage() {
        IndexSizePagination pagination = new IndexSizePagination(Optional.of(2), Optional.of(10), 11);
        assertThat(pagination.getFirstEntryNumberOnThisPage(), equalTo(11));
    }

    @Test
    public void getLastEntryNumberOnThisPage_returnsTheNumberOfLastEntryOnPage() {
        IndexSizePagination pagination = new IndexSizePagination(Optional.of(2), Optional.of(10), 12);
        assertThat(pagination.getLastEntryNumberOnThisPage(), equalTo(12));
    }

    @Test
    public void isSinglePage_retrunsTrue_whenPageContainsAllEntries() {
        IndexSizePagination pagination = new IndexSizePagination(Optional.of(1), Optional.of(100), 99);
        assertThat(pagination.isSinglePage(), equalTo(true));
        pagination = new IndexSizePagination(Optional.of(1), Optional.of(100), 100);
        assertThat(pagination.isSinglePage(), equalTo(true));
    }

    @Test
    public void isSinglePage_retrunsFalse_whenPageDoesNotContainsAllEntries() {
        IndexSizePagination pagination = new IndexSizePagination(Optional.of(2), Optional.of(50), 99);
        assertThat(pagination.isSinglePage(), equalTo(false));
    }

}
