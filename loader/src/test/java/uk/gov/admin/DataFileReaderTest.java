package uk.gov.admin;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("ConstantConditions")
public class DataFileReaderTest {
    List<String> expectedData = Collections.singletonList("{\"address\":\"0000001\",\"postcode\":\"01010101\"}");

    Path testFilePath;

    @Before
    public void setUp() throws Exception {
        testFilePath = Files.createTempFile("test-load", "");
    }

    @After
    public void cleanup() throws IOException {
        Files.deleteIfExists(testFilePath);
    }

    @Test
    public void should_be_able_to_read_local_file_contains_json_entries() throws IOException {
        Files.write(testFilePath, "{\"address\":\"0000001\",\"postcode\":\"01010101\"}".getBytes());

        Iterator<String> entriesIterator = new DataFileReader(testFilePath.toAbsolutePath().toString(), "jsonl").getFileEntriesIterator();

        assertEquals(expectedData, listFrom(entriesIterator));
    }

    @Test
    public void should_be_able_to_read_local_file_contains_csv_entries() throws IOException {

        Files.write(testFilePath, "address,postcode\n0000001,01010101".getBytes());

        Iterator<String> entriesIterator = new DataFileReader(testFilePath.toAbsolutePath().toString(), "csv").getFileEntriesIterator();

        assertEquals(expectedData, listFrom(entriesIterator));
    }

    @Test
    public void should_be_able_to_read_local_file_contains_tsv_entries() throws IOException {


        Files.write(testFilePath, "address\tpostcode\n0000001\t01010101".getBytes());

        Iterator<String> entriesIterator = new DataFileReader(testFilePath.toAbsolutePath().toString(), "tsv").getFileEntriesIterator();

        assertEquals(expectedData, listFrom(entriesIterator));
    }


    protected List<String> listFrom(Iterator<String> entriesIterator) {
        List<String> data = new ArrayList<>();

        while (entriesIterator.hasNext()) {
            data.add(entriesIterator.next());
        }
        return data;
    }
}


