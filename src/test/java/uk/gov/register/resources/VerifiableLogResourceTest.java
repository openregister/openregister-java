package uk.gov.register.resources;

import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Test;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.util.HashValue;
import uk.gov.register.views.ConsistencyProof;
import uk.gov.register.views.EntryProof;
import uk.gov.register.views.RegisterProof;

import javax.ws.rs.BadRequestException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

public class VerifiableLogResourceTest {
    private final String sampleHash1 = "6b85b168f7c5f0587fc22ff4ba6937e61b33f6e89b70eed53d78d895d35dc9c3";
    private final String sampleHash2 = "d3d33f57b033d18ad11e14b28ef6f33487410c98548d1759c772370dfeb6db11";

    @Test
    public void shouldUseServiceToGetRegisterProof() throws NoSuchAlgorithmException {
        RegisterProof expectedProof = new RegisterProof(new HashValue(HashingAlgorithm.SHA256, sampleHash1));
        RegisterReadOnly registerMock = mock(RegisterReadOnly.class);
        when(registerMock.getRegisterProof()).thenReturn(expectedProof);

        VerifiableLogResource vlResource = new VerifiableLogResource(registerMock);
        RegisterProof actualProof = vlResource.registerProof();

        verify(registerMock, times(1)).getRegisterProof();
        assertThat(actualProof.getProofIdentifier(), equalTo(expectedProof.getProofIdentifier()));
        assertThat(actualProof.getRootHash(), equalTo(expectedProof.getRootHash()));
    }

    @Test
    public void shouldUseServiceToGetEntryProof() throws NoSuchAlgorithmException {
        int entryNumber = 2;
        int totalEntries = 5;
        HashValue expectedHash1 = new HashValue(HashingAlgorithm.SHA256, sampleHash1);
        HashValue expectedHash2 = new HashValue(HashingAlgorithm.SHA256, sampleHash2);
        List<HashValue> expectedAuditPath = Arrays.asList(expectedHash1, expectedHash2);

        EntryProof expectedProof = new EntryProof("3", expectedAuditPath);
        RegisterReadOnly registerMock = mock(RegisterReadOnly.class);
        when(registerMock.getTotalEntries()).thenReturn(8);
        when(registerMock.getEntryProof(entryNumber, totalEntries)).thenReturn(expectedProof);

        VerifiableLogResource vlResource = new VerifiableLogResource(registerMock);
        EntryProof actualProof = vlResource.entryProof(entryNumber, totalEntries);

        verify(registerMock, times(1)).getEntryProof(entryNumber, totalEntries);
        assertThat(actualProof.getProofIdentifier(), equalTo(expectedProof.getProofIdentifier()));
        assertThat(actualProof.getEntryNumber(), equalTo(expectedProof.getEntryNumber()));
        assertThat(actualProof.getAuditPath(), IsIterableContainingInOrder.contains(expectedHash1, expectedHash2));
    }

    @Test
    public void shouldUseServiceToGetConsistencyProof() throws NoSuchAlgorithmException {
        int totalEntries1 = 3;
        int totalEntries2 = 6;
        HashValue expectedHash1 = new HashValue(HashingAlgorithm.SHA256, sampleHash1);
        HashValue expectedHash2 = new HashValue(HashingAlgorithm.SHA256, sampleHash2);
        List<HashValue> expectedConsistencyNodes = Arrays.asList(expectedHash1, expectedHash2);

        ConsistencyProof expectedProof = new ConsistencyProof(expectedConsistencyNodes);
        RegisterReadOnly registerMock = mock(RegisterReadOnly.class);
        when(registerMock.getTotalEntries()).thenReturn(8);
        when(registerMock.getConsistencyProof(totalEntries1, totalEntries2)).thenReturn(expectedProof);

        VerifiableLogResource vlResource = new VerifiableLogResource(registerMock);
        ConsistencyProof actualProof = vlResource.consistencyProof(totalEntries1, totalEntries2);

        verify(registerMock, times(1)).getConsistencyProof(totalEntries1, totalEntries2);
        assertThat(actualProof.getProofIdentifier(), equalTo(expectedProof.getProofIdentifier()));

        assertThat(actualProof.getConsistencyNodes(), IsIterableContainingInOrder.contains(expectedHash1, expectedHash2));
    }

    @Test(expected = BadRequestException.class)
    public void entryProofShouldThrowBadRequestExceptionIfEntryNumberTooSmall() throws NoSuchAlgorithmException {
        RegisterReadOnly registerMock = mock(RegisterReadOnly.class);
        when(registerMock.getTotalEntries()).thenReturn(8);
        VerifiableLogResource vlResource = new VerifiableLogResource(registerMock);

        vlResource.entryProof(0, 5);
    }

    @Test(expected = BadRequestException.class)
    public void entryProofShouldThrowBadRequestExceptionIfEntryNumberGreaterThanTotalEntries() throws NoSuchAlgorithmException {
        RegisterReadOnly registerMock = mock(RegisterReadOnly.class);
        when(registerMock.getTotalEntries()).thenReturn(8);
        VerifiableLogResource vlResource = new VerifiableLogResource(registerMock);

        vlResource.entryProof(5, 3);
    }

    @Test(expected = BadRequestException.class)
    public void entryProofShouldThrowBadRequestExceptionIfTotalEntriesGreaterThanSizeOfRegister() throws NoSuchAlgorithmException {
        RegisterReadOnly registerMock = mock(RegisterReadOnly.class);
        when(registerMock.getTotalEntries()).thenReturn(8);
        VerifiableLogResource vlResource = new VerifiableLogResource(registerMock);

        vlResource.entryProof(5, 10);
    }

    @Test(expected = BadRequestException.class)
    public void consistencyProofShouldThrowBadRequestExceptionIfTotalEntries1TooSmall() throws NoSuchAlgorithmException {
        RegisterReadOnly registerMock = mock(RegisterReadOnly.class);
        when(registerMock.getTotalEntries()).thenReturn(8);
        VerifiableLogResource vlResource = new VerifiableLogResource(registerMock);

        vlResource.consistencyProof(0, 5);
    }

    @Test(expected = BadRequestException.class)
    public void consistencyProofshouldThrowBadRequestExceptionIfTotalEntries2SmallerThanTotalEntries1() throws NoSuchAlgorithmException {
        RegisterReadOnly registerMock = mock(RegisterReadOnly.class);
        when(registerMock.getTotalEntries()).thenReturn(8);
        VerifiableLogResource vlResource = new VerifiableLogResource(registerMock);

        vlResource.consistencyProof(5, 3);
    }

    @Test(expected = BadRequestException.class)
    public void consistencyProofShouldThrowBadRequestExceptionIfTotalEntries2GreaterThanSizeOfRegister() throws NoSuchAlgorithmException {
        RegisterReadOnly registerMock = mock(RegisterReadOnly.class);
        when(registerMock.getTotalEntries()).thenReturn(8);
        VerifiableLogResource vlResource = new VerifiableLogResource(registerMock);

        vlResource.consistencyProof(5, 10);
    }
}

