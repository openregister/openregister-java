package uk.gov.admin;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DataReaderTest {
    @Test
    public void should_be_able_to_read_local_file() {
        final String expectedData = "{\"address\":\"0000001\",\"postcode\":\"01010101\"}\n";

        final String localfilePath = "test-load.jsonl";
        final String filePath = DataReaderTest.class.getClassLoader().getResource(localfilePath).getPath();
        DataReader dataReader = new DataReader(filePath);
        final String data = dataReader.data();

        assertEquals(expectedData, data);
    }
}


