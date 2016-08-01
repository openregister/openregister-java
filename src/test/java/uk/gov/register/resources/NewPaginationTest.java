package uk.gov.register.resources;

import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class NewPaginationTest {
    @Test
    public void hasPreviousPage_returnsTrueWhenPreviousPageExists() {
        assertThat(new NewPagination(Optional.of(10), Optional.empty(), 120).hasPreviousPage(), equalTo(true));
    }

    @Test
    public void hasPreviousPage_returnsFalseWhenPreviousPageNotExists() {
        assertThat(new NewPagination(Optional.empty(), Optional.empty(), 120).hasPreviousPage(), equalTo(false));
        assertThat(new NewPagination(Optional.of(-1), Optional.empty(), 120).hasPreviousPage(), equalTo(false));
    }

    @Test
    public void hasNextPage_returnsTrueWhenNextPageExists() {
        assertThat(new NewPagination(Optional.empty(), Optional.empty(), 120).hasNextPage(), equalTo(true));
        assertThat(new NewPagination(Optional.of(-1), Optional.empty(), 100).hasNextPage(), equalTo(true));
    }

    @Test
    public void hasNextPage_returnsFalseWhenNextPageNotExists() {
        assertThat(new NewPagination(Optional.of(1), Optional.of(90), 90).hasNextPage(), equalTo(false));
    }

    @Test
    public void getPreviousPageLink_returnsThePreviousPageLink() {
        assertThat(new NewPagination(Optional.of(50), Optional.of(10), 120).getPreviousPageLink(), equalTo("?start=40&limit=10"));
        assertThat(new NewPagination(Optional.of(10), Optional.of(20), 120).getPreviousPageLink(), equalTo("?start=-10&limit=20"));
    }

    @Test
    public void getNextPageLink_returnsTheNextPageLink() {
        assertThat(new NewPagination(Optional.of(50), Optional.of(10), 120).getNextPageLink(), equalTo("?start=60&limit=10"));
        assertThat(new NewPagination(Optional.of(11), Optional.of(100), 120).getNextPageLink(), equalTo("?start=111&limit=100"));
    }

    @Test
    public void getTotalPages_returnsTheNumberOfPages() {
        assertThat(new NewPagination(Optional.of(1), Optional.of(100), 204).getTotalPages(), equalTo(3));
        assertThat(new NewPagination(Optional.of(3), Optional.of(100), 204).getTotalPages(), equalTo(4));

        assertThat(new NewPagination(Optional.of(50), Optional.of(10), 120).getTotalPages(), equalTo(13));
        assertThat(new NewPagination(Optional.of(4), Optional.of(4), 16).getTotalPages(), equalTo(5));
        assertThat(new NewPagination(Optional.of(4), Optional.of(4), 15).getTotalPages(), equalTo(4));
        assertThat(new NewPagination(Optional.of(5), Optional.of(100), 1000).getTotalPages(), equalTo(11));
        assertThat(new NewPagination(Optional.of(1), Optional.of(100), 1000).getTotalPages(), equalTo(10));
        assertThat(new NewPagination(Optional.of(101), Optional.of(100), 1000).getTotalPages(), equalTo(10));


        assertThat(new NewPagination(Optional.of(-1), Optional.of(4), 15).getTotalPages(), equalTo(5));
        assertThat(new NewPagination(Optional.of(-2), Optional.of(4), 15).getTotalPages(), equalTo(5));
        assertThat(new NewPagination(Optional.of(-3), Optional.of(4), 15).getTotalPages(), equalTo(4));
        assertThat(new NewPagination(Optional.of(-2), Optional.of(4), 16).getTotalPages(), equalTo(5));
        assertThat(new NewPagination(Optional.of(-7), Optional.of(4), 15).getTotalPages(), equalTo(4));

    }

    @Test
    public void getFirstEntryNumberOnThisPage_returnsTheFirstEntryNumber() {
        assertThat(new NewPagination(Optional.of(11), Optional.of(100), 100).getFirstEntryNumberOnThisPage(), equalTo(11));
        assertThat(new NewPagination(Optional.of(1), Optional.of(100), 100).getFirstEntryNumberOnThisPage(), equalTo(1));
        assertThat(new NewPagination(Optional.of(-1), Optional.of(100), 100).getFirstEntryNumberOnThisPage(), equalTo(1));
        assertThat(new NewPagination(Optional.of(0), Optional.of(100), 100).getFirstEntryNumberOnThisPage(), equalTo(1));
    }

    @Test
    public void getLastEntryNumberOnThisPage_returnsTheLastEntryNumber() {
        assertThat(new NewPagination(Optional.of(11), Optional.of(100), 90).getLastEntryNumberOnThisPage(), equalTo(90));
        assertThat(new NewPagination(Optional.of(11), Optional.of(100), 120).getLastEntryNumberOnThisPage(), equalTo(110));
    }

    @Test
    public void getPreviousPageNumber_returnsThePreviousPageNumber() {
        assertThat(new NewPagination(Optional.of(11), Optional.of(100), 100).getPreviousPageNumber(), equalTo(1));
        assertThat(new NewPagination(Optional.of(102), Optional.of(100), 120).getPreviousPageNumber(), equalTo(2));
    }

    @Test
    public void getNextPageNumber_returnsTheNextPageNumber() {
        assertThat(new NewPagination(Optional.of(-1), Optional.of(100), 100).getNextPageNumber(), equalTo(2));
        assertThat(new NewPagination(Optional.of(11), Optional.of(100), 100).getNextPageNumber(), equalTo(3));
        assertThat(new NewPagination(Optional.of(121), Optional.of(100), 250).getNextPageNumber(), equalTo(4));
    }

    @Test
    public void isSinglePage_returnsTrueIfOnlySinglePageExists() {
        assertThat(new NewPagination(Optional.of(1), Optional.of(100), 100).isSinglePage(), equalTo(true));
        assertThat(new NewPagination(Optional.of(1), Optional.of(2), 2).isSinglePage(), equalTo(true));
    }

    @Test
    public void isSinglePage_returnsFalseIfMoreThanOnePageExists() {
        assertThat(new NewPagination(Optional.of(1), Optional.of(100), 101).isSinglePage(), equalTo(false));
        assertThat(new NewPagination(Optional.of(1), Optional.of(2), 4).isSinglePage(), equalTo(false));
        assertThat(new NewPagination(Optional.of(2), Optional.of(2), 2).isSinglePage(), equalTo(false));
    }
}