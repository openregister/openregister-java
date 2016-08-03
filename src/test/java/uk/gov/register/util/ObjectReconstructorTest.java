package uk.gov.register.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.register.util.ObjectReconstructor;

@RunWith(MockitoJUnitRunner.class)
public class ObjectReconstructorTest {
    @Test
    public void createsJsonNodeFromValidJson() {
        ObjectReconstructor testObj = new ObjectReconstructor();
        String payload = "{\"register\":\"value1\"}";
        testObj.reconstruct(payload.split("\n"));
    }
}
