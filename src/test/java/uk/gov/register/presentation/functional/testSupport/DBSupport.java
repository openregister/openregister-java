package uk.gov.register.presentation.functional.testSupport;

import uk.gov.register.presentation.functional.TestEntry;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.register.presentation.functional.TestEntry.anEntry;

public class DBSupport {
    public static void main(String[] args) throws IOException, SQLException {
        String filePath = args[1];

        DBSupport dbSupport = new DBSupport(TestDAO.get(args[2], "postgres"));
        try (Stream<String> lines = Files.lines(new File(filePath).toPath(), Charset.defaultCharset())) {
            AtomicInteger index = new AtomicInteger(0);
            dbSupport.publishEntries(args[0], lines.map(l -> anEntry(index.incrementAndGet(), l)).collect(Collectors.toList()));
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

    private void insertIntoItemAndEntryTables(TestEntry testEntry) {
        testDAO.testEntryDAO.insert(testEntry.entryNumber, testEntry.sha256hex, testEntry.entryTimestamp);
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
