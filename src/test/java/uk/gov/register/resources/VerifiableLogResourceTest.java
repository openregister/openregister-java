package uk.gov.register.resources;

import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Test;
import uk.gov.register.core.EntryLog;
import uk.gov.register.core.EntryType;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.resources.v2.VerifiableLogResource;
import uk.gov.register.util.HashValue;
import uk.gov.register.views.ConsistencyProof;
import uk.gov.register.views.EntryProof;
import uk.gov.register.views.RegisterProof;

import javax.ws.rs.BadRequestException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

public class VerifiableLogResourceTest {
    private final String sampleHash1 = "6b85b168f7c5f0587fc22ff4ba6937e61b33f6e89b70eed53d78d895d35dc9c3";
    private final String sampleHash2 = "d3d33f57b033d18ad11e14b28ef6f33487410c98548d1759c772370dfeb6db11";

    @Test
    public void shouldUseServiceToGetRegisterProof() {
        RegisterProof expectedProof = new RegisterProof(new HashValue(HashingAlgorithm.SHA256, sampleHash1), 12345);
        EntryLog entryLogMock = mock(EntryLog.class);
        when(entryLogMock.getV1RegisterProof()).thenReturn(expectedProof);

        VerifiableLogResource vlResource = new VerifiableLogResource(entryLogMock);
        RegisterProof actualProof = vlResource.registerProof();

        verify(entryLogMock, times(1)).getV1RegisterProof();
        assertThat(actualProof.getProofIdentifier(), equalTo(expectedProof.getProofIdentifier()));
        assertThat(actualProof.getRootHash(), equalTo(expectedProof.getRootHash()));
    }

    @Test
    public void shouldUseServiceToGetEntryProof() {
        int entryNumber = 2;
        int totalEntries = 5;
        HashValue expectedHash1 = new HashValue(HashingAlgorithm.SHA256, sampleHash1);
        HashValue expectedHash2 = new HashValue(HashingAlgorithm.SHA256, sampleHash2);
        List<HashValue> expectedAuditPath = Arrays.asList(expectedHash1, expectedHash2);

        EntryProof expectedProof = new EntryProof("3", expectedAuditPath);
        EntryLog entryLogMock = mock(EntryLog.class);
        when(entryLogMock.getTotalEntries(EntryType.user)).thenReturn(8);
        when(entryLogMock.getV1EntryProof(entryNumber, totalEntries)).thenReturn(expectedProof);

        VerifiableLogResource vlResource = new VerifiableLogResource(entryLogMock);
        EntryProof actualProof = vlResource.entryProof(entryNumber, totalEntries);

        verify(entryLogMock, times(1)).getV1EntryProof(entryNumber, totalEntries);
        assertThat(actualProof.getProofIdentifier(), equalTo(expectedProof.getProofIdentifier()));
        assertThat(actualProof.getEntryNumber(), equalTo(expectedProof.getEntryNumber()));
        assertThat(actualProof.getAuditPath(), IsIterableContainingInOrder.contains(expectedHash1, expectedHash2));
    }

    @Test
    public void shouldUseServiceToGetConsistencyProof() {
        int totalEntries1 = 3;
        int totalEntries2 = 6;
        HashValue expectedHash1 = new HashValue(HashingAlgorithm.SHA256, sampleHash1);
        HashValue expectedHash2 = new HashValue(HashingAlgorithm.SHA256, sampleHash2);
        List<HashValue> expectedConsistencyNodes = Arrays.asList(expectedHash1, expectedHash2);

        ConsistencyProof expectedProof = new ConsistencyProof(expectedConsistencyNodes);
        EntryLog entryLogMock = mock(EntryLog.class);
        when(entryLogMock.getTotalEntries(EntryType.user)).thenReturn(8);
        when(entryLogMock.getV1ConsistencyProof(totalEntries1, totalEntries2)).thenReturn(expectedProof);

        VerifiableLogResource vlResource = new VerifiableLogResource(entryLogMock);
        ConsistencyProof actualProof = vlResource.consistencyProof(totalEntries1, totalEntries2);

        verify(entryLogMock, times(1)).getV1ConsistencyProof(totalEntries1, totalEntries2);
        assertThat(actualProof.getProofIdentifier(), equalTo(expectedProof.getProofIdentifier()));

        assertThat(actualProof.getConsistencyNodes(), IsIterableContainingInOrder.contains(expectedHash1, expectedHash2));
    }

    @Test(expected = BadRequestException.class)
    public void entryProofShouldThrowBadRequestExceptionIfEntryNumberTooSmall() {
        EntryLog entryLogMock = mock(EntryLog.class);
        when(entryLogMock.getTotalEntries(EntryType.user)).thenReturn(8);
        VerifiableLogResource vlResource = new VerifiableLogResource(entryLogMock);

        vlResource.entryProof(0, 5);
    }

    @Test(expected = BadRequestException.class)
    public void entryProofShouldThrowBadRequestExceptionIfEntryNumberGreaterThanTotalEntries() {
        EntryLog entryLogMock = mock(EntryLog.class);
        when(entryLogMock.getTotalEntries(EntryType.user)).thenReturn(8);
        VerifiableLogResource vlResource = new VerifiableLogResource(entryLogMock);

        vlResource.entryProof(5, 3);
    }

    @Test(expected = BadRequestException.class)
    public void entryProofShouldThrowBadRequestExceptionIfTotalEntriesGreaterThanSizeOfRegister() {
        EntryLog entryLogMock = mock(EntryLog.class);
        when(entryLogMock.getTotalEntries(EntryType.user)).thenReturn(8);
        VerifiableLogResource vlResource = new VerifiableLogResource(entryLogMock);

        vlResource.entryProof(5, 10);
    }

    @Test(expected = BadRequestException.class)
    public void consistencyProofShouldThrowBadRequestExceptionIfTotalEntries1TooSmall() {
        EntryLog entryLogMock = mock(EntryLog.class);
        when(entryLogMock.getTotalEntries(EntryType.user)).thenReturn(8);
        VerifiableLogResource vlResource = new VerifiableLogResource(entryLogMock);

        vlResource.consistencyProof(0, 5);
    }

    @Test(expected = BadRequestException.class)
    public void consistencyProofshouldThrowBadRequestExceptionIfTotalEntries2SmallerThanTotalEntries1() {
        EntryLog entryLogMock = mock(EntryLog.class);
        when(entryLogMock.getTotalEntries(EntryType.user)).thenReturn(8);
        VerifiableLogResource vlResource = new VerifiableLogResource(entryLogMock);

        vlResource.consistencyProof(5, 3);
    }

    @Test(expected = BadRequestException.class)
    public void consistencyProofShouldThrowBadRequestExceptionIfTotalEntries2GreaterThanSizeOfRegister() {
        EntryLog entryLogMock = mock(EntryLog.class);
        when(entryLogMock.getTotalEntries(EntryType.user)).thenReturn(8);
        VerifiableLogResource vlResource = new VerifiableLogResource(entryLogMock);

        vlResource.consistencyProof(5, 10);
    }
}

