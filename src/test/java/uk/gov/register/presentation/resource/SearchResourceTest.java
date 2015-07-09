package uk.gov.register.presentation.resource;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.register.presentation.dao.RecentEntryIndexQueryDAO;

import java.util.Collections;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SearchResourceTest {
    @Mock
    private RecentEntryIndexQueryDAO recentEntryIndexQueryDAO;

    @Test
    public void findByPrimaryKey_returnsTheEntryJsonForGivenPrimaryKeyAndValue() {

        SearchResource resource = new SearchResource(recentEntryIndexQueryDAO);

        JsonNode jsonNode = mock(JsonNode.class);

        when(recentEntryIndexQueryDAO.find("primaryKey", "value")).thenReturn(Collections.singletonList(jsonNode));

        JsonNode result = resource.findByPrimaryKey("primaryKey", "value");

        assertThat(result, is(jsonNode));
    }

}