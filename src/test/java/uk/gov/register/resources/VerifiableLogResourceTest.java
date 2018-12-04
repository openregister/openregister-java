package uk.gov.register.resources;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.register.core.EntryLog;
import uk.gov.register.core.EntryType;
import uk.gov.register.core.RegisterContext;
import uk.gov.register.resources.v2.VerifiableLogResource;

import javax.ws.rs.BadRequestException;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class VerifiableLogResourceTest {
    private final String sampleHash1 = "6b85b168f7c5f0587fc22ff4ba6937e61b33f6e89b70eed53d78d895d35dc9c3";
    private final String sampleHash2 = "d3d33f57b033d18ad11e14b28ef6f33487410c98548d1759c772370dfeb6db11";

    @Mock
    private RegisterContext registerContext;

    @Mock
    private EntryLog entryLog;

    private VerifiableLogResource resource;

    @Before
    public void setUp() {
        when(registerContext.buildEntryLog()).thenReturn(entryLog);

        resource = new VerifiableLogResource(registerContext);
    }

    @Test(expected = BadRequestException.class)
    public void entryProofShouldThrowBadRequestExceptionIfEntryNumberTooSmall() {
        resource.entryProof(0, 5);
    }

    @Test(expected = BadRequestException.class)
    public void entryProofShouldThrowBadRequestExceptionIfEntryNumberGreaterThanTotalEntries() {
        resource.entryProof(5, 3);
    }

    @Test(expected = BadRequestException.class)
    public void entryProofShouldThrowBadRequestExceptionIfTotalEntriesGreaterThanSizeOfRegister() {
        when(entryLog.getTotalEntries(EntryType.user)).thenReturn(8);

        resource.entryProof(5, 10);
    }

    @Test(expected = BadRequestException.class)
    public void consistencyProofShouldThrowBadRequestExceptionIfTotalEntries1TooSmall() {
        resource.consistencyProof(0, 5);
    }

    @Test(expected = BadRequestException.class)
    public void consistencyProofshouldThrowBadRequestExceptionIfTotalEntries2SmallerThanTotalEntries1() {
        resource.consistencyProof(5, 3);
    }

    @Test(expected = BadRequestException.class)
    public void consistencyProofShouldThrowBadRequestExceptionIfTotalEntries2GreaterThanSizeOfRegister() {
        when(entryLog.getTotalEntries(EntryType.user)).thenReturn(8);

        resource.consistencyProof(5, 10);
    }
}

