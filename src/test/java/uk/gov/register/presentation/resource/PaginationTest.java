package uk.gov.register.presentation.resource;

import org.junit.Test;

import javax.ws.rs.BadRequestException;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class PaginationTest {

    @Test(expected = BadRequestException.class)
    public void construct_throwsExceptionWhenPageSizeIsZero() {
        new Pagination(Optional.of(1l), Optional.of(0l), 10);
    }

    @Test(expected = BadRequestException.class)
    public void construct_throwsExceptionWhenPageSizeIsNegativeNumber() {
        new Pagination(Optional.of(1l), Optional.of(-1l), 10);
    }

    @Test(expected = BadRequestException.class)
    public void construct_throwsExceptionWhenPageIndexIsZero() {
        new Pagination(Optional.of(0l), Optional.of(1l), 10);
    }

    @Test(expected = BadRequestException.class)
    public void construct_throwsExceptionWhenPageIndexIsNegativeNumber() {
        new Pagination(Optional.of(-1l), Optional.of(1l), 10);
    }


    @Test
    public void offset_returnsTheNumberWhichOffsetsTheTotalEntriesBasedOnPageSize() {
        assertThat(new Pagination(Optional.of(1l), Optional.of(10l), 100).offset(), equalTo(0l));
        assertThat(new Pagination(Optional.of(2l), Optional.of(10l), 100).offset(), equalTo(10l));
    }

    @Test
    public void hasNextPage_returnsTrueOnlyWhenThereAreMoreEntriesAvailable() {
        assertFalse(new Pagination(Optional.of(1l), Optional.of(10l), 10).hasNextPage());
        assertFalse(new Pagination(Optional.of(2l), Optional.of(10l), 20).hasNextPage());

        assertTrue(new Pagination(Optional.of(1l), Optional.of(10l), 11).hasNextPage());

        assertTrue(new Pagination(Optional.of(2l), Optional.of(10l), 21).hasNextPage());
    }

    @Test
    public void hasPreviousPage_returnsTrueOnlyWhenPageIndexIsMopreThanOne() {
        assertFalse(new Pagination(Optional.of(1l), Optional.of(10l), 10).hasPreviousPage());
        assertFalse(new Pagination(Optional.of(1l), Optional.of(10l), 11).hasPreviousPage());

        assertTrue(new Pagination(Optional.of(2l), Optional.of(10l), 11).hasPreviousPage());
    }
}
