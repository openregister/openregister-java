package uk.gov.register.indexer.function;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import uk.gov.register.core.*;
import uk.gov.register.indexer.IndexKeyItemPair;
import uk.gov.register.store.DataAccessLayer;
import uk.gov.register.util.HashValue;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CurrentCountriesIndexFunctionTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setup(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void executeWithKeyAndHash_shouldReturnEmptySet_whenItemDoesNotExist() {
        DataAccessLayer dataAccessLayer = mock(DataAccessLayer.class);
        when(dataAccessLayer.getItem(any())).thenReturn(Optional.empty());

        CurrentCountriesIndexFunction func = new CurrentCountriesIndexFunction("current-countries");
        Set<IndexKeyItemPair> resultSet = new HashSet<>();
        func.execute(h -> dataAccessLayer.getItem(h), EntryType.user, "CS", new HashValue(HashingAlgorithm.SHA256, "cs"), resultSet);

        assertThat(resultSet, is(empty()));
    }

    @Test
    public void executeWithKeyAndHash_shouldReturnIndexItemValuePairByCountry_whenCountryIsCurrent() throws IOException {
        HashValue itemHashVN = new HashValue(HashingAlgorithm.SHA256, "vn");
        Item countryVN = new Item(itemHashVN, objectMapper.readTree("{\"country\":\"VN\",\"name\":\"Vietnam\"}"));
        DataAccessLayer dataAccessLayer = mock(DataAccessLayer.class);
        when(dataAccessLayer.getItem(itemHashVN)).thenReturn(Optional.of(countryVN));

        CurrentCountriesIndexFunction func = new CurrentCountriesIndexFunction("current-countries");
        Set<IndexKeyItemPair> resultSet = new HashSet<>();
        func.execute(h -> dataAccessLayer.getItem(h), EntryType.user, "VN", itemHashVN, resultSet);

        assertThat(resultSet.size(), is(1));
        assertThat(resultSet, contains(new IndexKeyItemPair("VN", itemHashVN)));
    }

    @Test
    public void executeWithKeyAndHash_shouldReturnEmptySet_whenCountryHasEnded() throws IOException {
        HashValue itemHashCS = new HashValue(HashingAlgorithm.SHA256, "cs");
        Item countryCS = new Item(itemHashCS, objectMapper.readTree("{\"country\":\"CS\",\"name\":\"Czechoslovakia\",\"end-date\":\"1991-12-25\"}"));
        DataAccessLayer dataAccessLayer = mock(DataAccessLayer.class);
        when(dataAccessLayer.getItem(itemHashCS)).thenReturn(Optional.of(countryCS));

        CurrentCountriesIndexFunction func = new CurrentCountriesIndexFunction("current-countries");
        Set<IndexKeyItemPair> resultSet = new HashSet<>();
        func.execute(h -> dataAccessLayer.getItem(h),EntryType.user,  "CS", itemHashCS, resultSet);

        assertThat(resultSet, is(empty()));
    }

    @Test
    public void executeWithEntry_shouldReturnCurrentCountriesByCountry_whenEntryContainsMultipleItems() throws IOException {
        HashValue itemHashVN = new HashValue(HashingAlgorithm.SHA256, "vn");
        HashValue itemHashCS = new HashValue(HashingAlgorithm.SHA256, "cs");
        Item countryVN = new Item(itemHashVN, objectMapper.readTree("{\"country\":\"VN\",\"name\":\"Vietnam\"}"));
        Item countryCS = new Item(itemHashCS, objectMapper.readTree("{\"country\":\"CS\",\"name\":\"Czechoslovakia\",\"end-date\":\"1991-12-25\"}"));
        Entry entry = new Entry(1, Arrays.asList(itemHashVN, itemHashCS), Instant.now(), "key", EntryType.user);
        DataAccessLayer dataAccessLayer = mock(DataAccessLayer.class);
        when(dataAccessLayer.getItem(itemHashVN)).thenReturn(Optional.of(countryVN));
        when(dataAccessLayer.getItem(itemHashCS)).thenReturn(Optional.of(countryCS));

        CurrentCountriesIndexFunction func = new CurrentCountriesIndexFunction("current-countries");
        Set<IndexKeyItemPair> resultSet = func.execute(h -> dataAccessLayer.getItem(h), entry);

        assertThat(resultSet.size(), is(1));
        assertThat(resultSet, contains(new IndexKeyItemPair("key", itemHashVN)));
    }
}
