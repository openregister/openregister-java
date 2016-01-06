package uk.gov.mint;

import com.fasterxml.jackson.core.JsonParseException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ObjectReconstructorTest {

    @Test(expected = JsonParseException.class)
    public void handle_throwsJsonParseExceptionWhenTheInputIsNotValidJsonl() {
        ObjectReconstructor testObj = new ObjectReconstructor();
        String payload = "{\"register\":\n\"value1\"}";
        testObj.reconstruct(payload.split("\n"));
    }

    @Test
    public void createsJsonNodeFromValidJson() {
        ObjectReconstructor testObj = new ObjectReconstructor();
        String payload = "{\"register\":\"value1\"}";
        testObj.reconstruct(payload.split("\n"));
    }
}
