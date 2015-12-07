package uk.gov.register.presentation.resource;

import org.junit.Test;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class PaginationTest {

    @Test(expected = BadRequestException.class)
    public void construct_throwsExceptionForPageSizeGreaterThan5000() {
        new Pagination("/entries", Optional.of(1L), Optional.of(5001L), 10);
    }

    @Test(expected = BadRequestException.class)
    public void construct_throwsExceptionWhenPageSizeIsZero() {
        new Pagination("/entries", Optional.of(1L), Optional.of(0L), 10);
    }

    @Test(expected = BadRequestException.class)
    public void construct_throwsExceptionWhenPageSizeIsNegativeNumber() {
        new Pagination("/entries", Optional.of(1L), Optional.of(-1L), 10);
    }

    @Test(expected = BadRequestException.class)
    public void construct_throwsExceptionWhenPageIndexIsZero() {
        new Pagination("/entries", Optional.of(0L), Optional.of(1L), 10);
    }

    @Test(expected = BadRequestException.class)
    public void construct_throwsExceptionWhenPageIndexIsNegativeNumber() {
        new Pagination("/entries", Optional.of(-1L), Optional.of(1L), 10);
    }

    @Test(expected = NotFoundException.class)
    public void construct_throwsNotFoundException_whenNoMoreEntriesForGivenPageSizeAndPageIndexValues() {
        new Pagination("/entries", Optional.of(2L), Optional.of(10L), 10);
    }

    @Test
    public void offset_returnsTheNumberWhichOffsetsTheTotalEntriesBasedOnPageSize() {
        assertThat(new Pagination("/entries", Optional.of(1L), Optional.of(10L), 100).offset(), equalTo(0L));
        assertThat(new Pagination("/entries", Optional.of(2L), Optional.of(10L), 100).offset(), equalTo(10L));
    }

    @Test
    public void hasNextPage_returnsTrueOnlyWhenThereAreMoreEntriesAvailable() {
        assertFalse(new Pagination("/entries", Optional.of(1L), Optional.of(10L), 10).hasNextPage());
        assertFalse(new Pagination("/entries", Optional.of(2L), Optional.of(10L), 20).hasNextPage());

        assertTrue(new Pagination("/entries", Optional.of(1L), Optional.of(10L), 11).hasNextPage());

        assertTrue(new Pagination("/entries", Optional.of(2L), Optional.of(10L), 21).hasNextPage());
    }

    @Test
    public void hasPreviousPage_returnsTrueOnlyWhenPageIndexIsMoreThanOne() {
        assertFalse(new Pagination("/entries", Optional.of(1L), Optional.of(10L), 10).hasPreviousPage());
        assertFalse(new Pagination("/entries", Optional.of(1L), Optional.of(10L), 11).hasPreviousPage());

        assertTrue(new Pagination("/entries", Optional.of(2L), Optional.of(10L), 11).hasPreviousPage());
    }

    @Test
    public void getTotalPages_returnsTotalNumberOfPages() {
        Pagination pagination = new Pagination(
                "/entries",
                Optional.of(1L),
                Optional.of(10L),
                10);
        assertThat(pagination.getTotalPages(), equalTo(1L));


        pagination = new Pagination(
                "/entries",
                Optional.of(1L),
                Optional.of(10L),
                11);
        assertThat(pagination.getTotalPages(), equalTo(2L));

    }

    @Test
    public void getNextPageLink_returnsTheLinkForNextPage() {
        Pagination pagination = new Pagination("/entries", Optional.of(1L), Optional.of(10L), 11);
        assertThat(pagination.getNextPageLink(), equalTo("/entries?page-index=2&page-size=10"));
    }

    @Test
    public void getPreviousPageLink_returnsTheLinkForPreviousPage() {
        Pagination pagination = new Pagination("/entries", Optional.of(2L), Optional.of(10L), 11);
        assertThat(pagination.getPreviousPageLink(), equalTo("/entries?page-index=1&page-size=10"));
    }

    @Test
    public void getFirstEntryNumberOnThisPage_returnsTheNumberOfFirstEntryOnPage() {
        Pagination pagination = new Pagination("/entries", Optional.of(2L), Optional.of(10L), 11);
        assertThat(pagination.getFirstEntryNumberOnThisPage(), equalTo(11L));
    }

    @Test
    public void getLastEntryNumberOnThisPage_returnsTheNumberOfLastEntryOnPage() {
        Pagination pagination = new Pagination("/entries", Optional.of(2L), Optional.of(10L), 12);
        assertThat(pagination.getLastEntryNumberOnThisPage(), equalTo(12L));
    }

    @Test
    public void isSinglePage_retrunsTrue_whenPageContainsAllEntries() {
        Pagination pagination = new Pagination("/entries", Optional.of(1L), Optional.of(100L), 99);
        assertThat(pagination.isSinglePage(), equalTo(true));
        pagination = new Pagination("/entries", Optional.of(1L), Optional.of(100L), 100);
        assertThat(pagination.isSinglePage(), equalTo(true));
    }

    @Test
    public void isSinglePage_retrunsFalse_whenPageDoesNotContainsAllEntries() {
        Pagination pagination = new Pagination("/entries", Optional.of(2L), Optional.of(50L), 99);
        assertThat(pagination.isSinglePage(), equalTo(false));
    }

}
