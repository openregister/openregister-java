package uk.gov.register.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

@RunWith(MockitoJUnitRunner.class)
public class ObjectReconstructorTest {
    @Test
    public void createsJsonNodeFromValidJson() throws IOException {
        ObjectReconstructor testObj = new ObjectReconstructor();
        String payload = "{\"register\":\"value1\"}";
        testObj.reconstruct(payload);
    }

    @Test
    public void createsJsonNodeFromValidJsonStringArray() {
        ObjectReconstructor testObj = new ObjectReconstructor();
        String payload = "{\"register\":\"value1\"}";
        testObj.reconstructWithCanonicalization(payload.split("\n"));
    }
}