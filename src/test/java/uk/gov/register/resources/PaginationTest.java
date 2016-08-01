package uk.gov.register.resources;

import org.junit.Test;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class PaginationTest {

    @Test(expected = BadRequestException.class)
    public void construct_throwsExceptionForPageSizeGreaterThan5000() {
        new Pagination(Optional.of(1), Optional.of(5001), 10);
    }

    @Test(expected = BadRequestException.class)
    public void construct_throwsExceptionWhenPageSizeIsZero() {
        new Pagination(Optional.of(1), Optional.of(0), 10);
    }

    @Test(expected = BadRequestException.class)
    public void construct_throwsExceptionWhenPageSizeIsNegativeNumber() {
        new Pagination(Optional.of(1), Optional.of(-1), 10);
    }

    @Test(expected = BadRequestException.class)
    public void construct_throwsExceptionWhenPageIndexIsZero() {
        new Pagination(Optional.of(0), Optional.of(1), 10);
    }

    @Test(expected = BadRequestException.class)
    public void construct_throwsExceptionWhenPageIndexIsNegativeNumber() {
        new Pagination(Optional.of(-1), Optional.of(1), 10);
    }

    @Test(expected = NotFoundException.class)
    public void construct_throwsNotFoundException_whenNoMoreEntriesForGivenPageSizeAndPageIndexValues() {
        new Pagination(Optional.of(2), Optional.of(10), 10);
    }

    @Test
    public void offset_returnsTheNumberWhichOffsetsTheTotalEntriesBasedOnPageSize() {
        assertThat(new Pagination(Optional.of(1), Optional.of(10), 100).offset(), equalTo(0));
        assertThat(new Pagination(Optional.of(2), Optional.of(10), 100).offset(), equalTo(10));
    }

    @Test
    public void hasNextPage_returnsTrueOnlyWhenThereAreMoreEntriesAvailable() {
        assertFalse(new Pagination(Optional.of(1), Optional.of(10), 10).hasNextPage());
        assertFalse(new Pagination(Optional.of(2), Optional.of(10), 20).hasNextPage());

        assertTrue(new Pagination(Optional.of(1), Optional.of(10), 11).hasNextPage());

        assertTrue(new Pagination(Optional.of(2), Optional.of(10), 21).hasNextPage());
    }

    @Test
    public void hasPreviousPage_returnsTrueOnlyWhenPageIndexIsMoreThanOne() {
        assertFalse(new Pagination(Optional.of(1), Optional.of(10), 10).hasPreviousPage());
        assertFalse(new Pagination(Optional.of(1), Optional.of(10), 11).hasPreviousPage());

        assertTrue(new Pagination(Optional.of(2), Optional.of(10), 11).hasPreviousPage());
    }

    @Test
    public void getTotalPages_returnsTotalNumberOfPages() {
        Pagination pagination = new Pagination(
                Optional.of(1),
                Optional.of(10),
                10);
        assertThat(pagination.getTotalPages(), equalTo(1));


        pagination = new Pagination(
                Optional.of(1),
                Optional.of(10),
                11);
        assertThat(pagination.getTotalPages(), equalTo(2));

    }

    @Test
    public void getNextPageLink_returnsTheLinkForNextPage() {
        Pagination pagination = new Pagination(Optional.of(1), Optional.of(10), 11);
        assertThat(pagination.getNextPageLink(), equalTo("?page-index=2&page-size=10"));
    }

    @Test
    public void getPreviousPageLink_returnsTheLinkForPreviousPage() {
        Pagination pagination = new Pagination(Optional.of(2), Optional.of(10), 11);
        assertThat(pagination.getPreviousPageLink(), equalTo("?page-index=1&page-size=10"));
    }

    @Test
    public void getFirstEntryNumberOnThisPage_returnsTheNumberOfFirstEntryOnPage() {
        Pagination pagination = new Pagination(Optional.of(2), Optional.of(10), 11);
        assertThat(pagination.getFirstEntryNumberOnThisPage(), equalTo(11));
    }

    @Test
    public void getLastEntryNumberOnThisPage_returnsTheNumberOfLastEntryOnPage() {
        Pagination pagination = new Pagination(Optional.of(2), Optional.of(10), 12);
        assertThat(pagination.getLastEntryNumberOnThisPage(), equalTo(12));
    }

    @Test
    public void isSinglePage_retrunsTrue_whenPageContainsAllEntries() {
        Pagination pagination = new Pagination(Optional.of(1), Optional.of(100), 99);
        assertThat(pagination.isSinglePage(), equalTo(true));
        pagination = new Pagination(Optional.of(1), Optional.of(100), 100);
        assertThat(pagination.isSinglePage(), equalTo(true));
    }

    @Test
    public void isSinglePage_retrunsFalse_whenPageDoesNotContainsAllEntries() {
        Pagination pagination = new Pagination(Optional.of(2), Optional.of(50), 99);
        assertThat(pagination.isSinglePage(), equalTo(false));
    }

}
