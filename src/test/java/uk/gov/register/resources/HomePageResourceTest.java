package uk.gov.register.resources;

import org.junit.Test;
import uk.gov.register.db.EntryQueryDAO;
import uk.gov.register.db.RecordQueryDAO;
import uk.gov.register.service.VerifiableLogService;
import uk.gov.register.views.HomePageView;
import uk.gov.register.views.RegisterProof;
import uk.gov.register.views.ViewFactory;

import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Optional;

import static org.mockito.Mockito.*;

public class HomePageResourceTest {

    @Test
    public void shouldReturnPageViewWithValidValues() throws NoSuchAlgorithmException {
        int totalRecords = 5;
        int totalEntries = 6;
        Optional<Instant> lastUpdated = Optional.of(Instant.ofEpochMilli(1459241964336L));
        RegisterProof registerProof = new RegisterProof("6b85b168f7c5f0587fc22ff4ba6937e61b33f6e89b70eed53d78d895d35dc9c3");
        HomePageView homePageView = mock(HomePageView.class);

        RecordQueryDAO recordDAOMock = mock(RecordQueryDAO.class);
        EntryQueryDAO entryDAOMock = mock(EntryQueryDAO.class);
        VerifiableLogService verifiableLogServiceMock = mock(VerifiableLogService.class);
        ViewFactory viewFactoryMock = mock(ViewFactory.class);

        when(recordDAOMock.getTotalRecords()).thenReturn(totalRecords);
        when(entryDAOMock.getTotalEntries()).thenReturn(totalEntries);
        when(entryDAOMock.getLastUpdatedTime()).thenReturn(lastUpdated);
        when(verifiableLogServiceMock.getRegisterProof()).thenReturn(registerProof);
        when(viewFactoryMock.homePageView(totalRecords, totalEntries, lastUpdated, registerProof)).thenReturn(homePageView);

        HomePageResource homePageResource = new HomePageResource(viewFactoryMock, recordDAOMock, entryDAOMock, verifiableLogServiceMock);
        homePageResource.home();

        verify(recordDAOMock, times(1)).getTotalRecords();
        verify(entryDAOMock, times(1)).getTotalEntries();
        verify(verifiableLogServiceMock, times(1)).getRegisterProof();
        verify(viewFactoryMock, times(1)).homePageView(totalRecords, totalEntries, lastUpdated, registerProof);
    }
}

