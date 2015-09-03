package uk.gov.admin;

import org.junit.Test;

import java.io.BufferedReader;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class DataReaderTest {
    @SuppressWarnings("ConstantConditions")
    @Test
    public void should_be_able_to_read_local_file() {
        List<String> expectedData = Collections.singletonList("{\"address\":\"0000001\",\"postcode\":\"01010101\"}");

        String localfilePath = "test-load.jsonl";

        String filePath = this.getClass().getClassLoader().getResource(localfilePath).getPath();
        BufferedReader reader = new DataReader(filePath).reader();
        List<String> data = reader.lines().collect(Collectors.toList());

        assertEquals(expectedData, data);
    }
}


