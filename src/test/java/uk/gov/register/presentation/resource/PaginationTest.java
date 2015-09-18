package uk.gov.register.presentation.resource;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class PaginationTest {
    @Test
    public void offset_returnsTheNumberWhichOffsetsTheTotalEntriesBasedOnPageSize() {
        assertThat(new Pagination(1, 10, 100).offset(), equalTo(0l));
        assertThat(new Pagination(2, 10, 100).offset(), equalTo(10l));
    }

    @Test
    public void hasNextPage_returnsTrueOnlyWhenThereAreMoreEntriesAvailable() {
        assertFalse(new Pagination(1, 10, 10).hasNextPage());
        assertFalse(new Pagination(2, 10, 20).hasNextPage());

        assertTrue(new Pagination(1, 10, 11).hasNextPage());

        assertTrue(new Pagination(2, 10, 21).hasNextPage());
    }

    @Test
    public void hasPreviousPage_returnsTrueOnlyWhenPageIndexIsMopreThanOne() {
        assertFalse(new Pagination(1, 10, 10).hasPreviousPage());
        assertFalse(new Pagination(1, 10, 11).hasPreviousPage());

        assertTrue(new Pagination(2, 10, 11).hasPreviousPage());
    }

}
