package uk.gov.admin;

import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DataReaderTest {
    @Test
    public void should_be_able_to_read_local_file() throws IOException {
        final List<String> expectedData = Arrays.asList("{\"address\":\"0000001\",\"postcode\":\"01010101\"}");

        final String localfilePath = "test-load.jsonl";
        final String filePath = DataReaderTest.class.getClassLoader().getResource(localfilePath).getPath();
        DataReader dataReader = new DataReader(filePath);
        final List<String> data = dataReader.data();

        assertEquals(expectedData, data);
    }
}


