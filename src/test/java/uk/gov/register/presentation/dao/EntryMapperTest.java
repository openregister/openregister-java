package uk.gov.register.presentation.dao;

import org.junit.Test;
import org.mockito.Mockito;
import org.skife.jdbi.v2.StatementContext;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class EntryMapperTest {
    @Test
    public void map_returnsSameDateAndTimeInUTC() throws SQLException {
        String expected = "2016-07-15T10:00:00Z";

        Instant parse = Instant.parse(expected);
        Timestamp t = new Timestamp(parse.toEpochMilli());

        StatementContext ctxMock = Mockito.mock(StatementContext.class);
        ResultSet resultSetMock = Mockito.mock(ResultSet.class);
        when(resultSetMock.getString(anyString())).thenReturn("");
        when(resultSetMock.getTimestamp(eq("timestamp"), anyObject())).thenReturn(t);
        when(resultSetMock.getTimestamp(eq("timestamp"))).thenReturn(t);

        EntryMapper sutEntryMapper = new EntryMapper();
        Entry actualEntry = sutEntryMapper.map(0, resultSetMock, ctxMock);

        verify(resultSetMock, times(2)).getString(anyString());
        verify(resultSetMock, times(1)).getTimestamp(eq("timestamp"), anyObject());

        assertThat(actualEntry.getTimestamp(), is(expected));
    }
}
