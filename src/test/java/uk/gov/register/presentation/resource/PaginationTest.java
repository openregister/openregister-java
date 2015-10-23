package uk.gov.register.presentation.resource;

import org.junit.Test;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class PaginationTest {

    @Test(expected = BadRequestException.class)
    public void construct_throwsExceptionWhenPageSizeIsZero() {
        new Pagination("/feed", Optional.of(1l), Optional.of(0l), 10);
    }

    @Test(expected = BadRequestException.class)
    public void construct_throwsExceptionWhenPageSizeIsNegativeNumber() {
        new Pagination("/feed", Optional.of(1l), Optional.of(-1l), 10);
    }

    @Test(expected = BadRequestException.class)
    public void construct_throwsExceptionWhenPageIndexIsZero() {
        new Pagination("/feed", Optional.of(0l), Optional.of(1l), 10);
    }

    @Test(expected = BadRequestException.class)
    public void construct_throwsExceptionWhenPageIndexIsNegativeNumber() {
        new Pagination("/feed", Optional.of(-1l), Optional.of(1l), 10);
    }

    @Test(expected = NotFoundException.class)
    public void construct_throwsNotFoundException_whenNoMoreEntriesForGivenPageSizeAndPageIndexValues() {
        new Pagination("/feed", Optional.of(2l), Optional.of(10l), 10);
    }

    @Test
    public void offset_returnsTheNumberWhichOffsetsTheTotalEntriesBasedOnPageSize() {
        assertThat(new Pagination("/feed", Optional.of(1l), Optional.of(10l), 100).offset(), equalTo(0l));
        assertThat(new Pagination("/feed", Optional.of(2l), Optional.of(10l), 100).offset(), equalTo(10l));
    }

    @Test
    public void hasNextPage_returnsTrueOnlyWhenThereAreMoreEntriesAvailable() {
        assertFalse(new Pagination("/feed", Optional.of(1l), Optional.of(10l), 10).hasNextPage());
        assertFalse(new Pagination("/feed", Optional.of(2l), Optional.of(10l), 20).hasNextPage());

        assertTrue(new Pagination("/feed", Optional.of(1l), Optional.of(10l), 11).hasNextPage());

        assertTrue(new Pagination("/feed", Optional.of(2l), Optional.of(10l), 21).hasNextPage());
    }

    @Test
    public void hasPreviousPage_returnsTrueOnlyWhenPageIndexIsMoreThanOne() {
        assertFalse(new Pagination("/feed", Optional.of(1l), Optional.of(10l), 10).hasPreviousPage());
        assertFalse(new Pagination("/feed", Optional.of(1l), Optional.of(10l), 11).hasPreviousPage());

        assertTrue(new Pagination("/feed", Optional.of(2l), Optional.of(10l), 11).hasPreviousPage());
    }

    @Test
    public void getTotalPages_returnsTotalNumberOfPages() {
        Pagination pagination = new Pagination(
                "/feed",
                Optional.of(1l),
                Optional.of(10l),
                10);
        assertThat(pagination.getTotalPages(), equalTo(1L));


        pagination = new Pagination(
                "/feed",
                Optional.of(1l),
                Optional.of(10l),
                11);
        assertThat(pagination.getTotalPages(), equalTo(2L));

    }

    @Test
    public void getNextPageLink_returnsTheLinkForNextPage() {
        Pagination pagination = new Pagination("/feed", Optional.of(1l), Optional.of(10l), 11);
        assertThat(pagination.getNextPageLink(), equalTo("/feed?pageIndex=2&pageSize=10"));
    }

    @Test
    public void getPreviousPageLink_returnsTheLinkForPreviousPage() {
        Pagination pagination = new Pagination("/feed", Optional.of(2l), Optional.of(10l), 11);
        assertThat(pagination.getPreviousPageLink(), equalTo("/feed?pageIndex=1&pageSize=10"));
    }
}
