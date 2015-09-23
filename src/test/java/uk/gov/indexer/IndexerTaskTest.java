package uk.gov.indexer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.indexer.dao.DestinationDBQueryDAO;
import uk.gov.indexer.dao.SourceDBQueryDAO;

import java.sql.SQLException;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class IndexerTaskTest {
    @Mock
    SourceDBQueryDAO sourceDBQueryDAO;

    @Mock
    DestinationDBQueryDAO destinationDBQueryDAO;

    @Mock
    List<byte[]> entries;

    @Test
    public void update_copiesAllEntriesFromSourceDBToDestinationDB_fetchedByWaterMark() throws SQLException {

        IndexerTask indexerTask = new IndexerTask("register", sourceDBQueryDAO, destinationDBQueryDAO);


        int currentWaterMark = 0;
        when(destinationDBQueryDAO.currentWaterMark()).thenReturn(currentWaterMark);
        when(sourceDBQueryDAO.read(currentWaterMark)).thenReturn(entries);

        indexerTask.update();

        verify(destinationDBQueryDAO).writeEntries(entries);
    }
}
