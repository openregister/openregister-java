package uk.gov.register.functional.db;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DBSupport {
    public static void main(String[] args) throws IOException, SQLException {

        String registerName = args[0];
        String filePath = args[1];
        String dbName = args[2];

        DBSupport dbSupport = new DBSupport(TestDAO.get(dbName, "postgres"));
        try (Stream<String> lines = Files.lines(new File(filePath).toPath(), Charset.defaultCharset())) {
            ObjectMapper objectMapper = new ObjectMapper();
            AtomicInteger index = new AtomicInteger(0);
            dbSupport.publishEntries(registerName, lines.map(l -> {
                try {
                    JsonNode jsonNode = objectMapper.readTree(l);
                    return TestEntry.anEntry(index.incrementAndGet(), l, jsonNode.get(registerName).asText());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList()));
        }
    }

    private TestDAO testDAO;

    public DBSupport(TestDAO testDAO) {
        this.testDAO = testDAO;
    }

    public void publishEntries(List<TestEntry> testEntries) {
        publishEntries("address", testEntries);
    }

    public void publishEntries(String registerName, List<TestEntry> testEntries) {
        testEntries.forEach(testEntry -> {
            insertIntoItemAndEntryTables(testEntry);
            updateOtherTables(registerName, testEntry.entryNumber, testEntry.itemJson);
        });
    }

    public void cleanDb(){
        testDAO.testEntryDAO.wipeData();
        testDAO.testItemCommandDAO.wipeData();
        testDAO.testRecordDAO.wipeData();
    }

    private void insertIntoItemAndEntryTables(TestEntry testEntry) {
        testDAO.testEntryDAO.insert(testEntry.entryNumber, testEntry.sha256hex, testEntry.getTimestampAsLong(), testEntry.itemKey);
        testDAO.testItemDAO.insertIfNotExist(testEntry.sha256hex, testEntry.itemJson);
    }

    private void updateOtherTables(String registerName, int serialNumber, String message) {
        String primaryKeyValue = extractRegisterKey(registerName, message);

        if (isSupersedingAnEntry(primaryKeyValue)) {
            testDAO.testCurrentKeyDAO.update(primaryKeyValue, serialNumber);
        } else {
            testDAO.testCurrentKeyDAO.insert(primaryKeyValue, serialNumber);
            testDAO.testTotalRecordDAO.updateBy(1);
        }
        testDAO.testTotalEntryDAO.updateBy(1);
    }

    private boolean isSupersedingAnEntry(String primaryKeyValue) {
        return testDAO.testCurrentKeyDAO.getSerialNumber(primaryKeyValue) != 0;
    }

    private String extractRegisterKey(String registerName, String message) {
        return message.replaceAll(".*\"" + registerName + "\"\\s*:\\s*\"([^\"]+)\".*", "$1");
    }
}
