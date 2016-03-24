package uk.gov.register.presentation.functional.testSupport;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Throwables;
import io.dropwizard.jackson.Jackson;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DBSupport {
    public static void main(String[] args) throws IOException, SQLException {
        String filePath = args[1];

        DBSupport dbSupport = new DBSupport(TestDAO.get(args[2], "postgres"));
        try (Stream<String> lines = Files.lines(new File(filePath).toPath(), Charset.defaultCharset())) {
            List<String> collect = lines.collect(Collectors.toList());
            dbSupport.publishMessages(args[0], collect);
        }
    }

    private TestDAO testDAO;

    public DBSupport(TestDAO testDAO) {
        this.testDAO = testDAO;
    }

    public void publishMessages(List<String> messages) {
        publishMessages("address", messages);
    }

    public void publishMessages(String registerName, List<String> messages) {
        SortedMap<Integer, String> messagesWithSerialNumbers = messages.stream().collect(Collectors.toMap(m -> messages.indexOf(m) + 1, m -> m, (a, b) -> a, TreeMap::new));
        publishMessages(registerName, messagesWithSerialNumbers);
    }

    public void publishMessages(String registerName, SortedMap<Integer, String> messages) {
        for (SortedMap.Entry<Integer, String> entry : messages.entrySet()) {
            testDAO.testEntryIndexDAO.insert(entry.getKey(), entry.getValue());
            insertIntoItemAndEntryTables(entry.getKey(), entry.getValue());
            updateOtherTables(registerName, entry.getKey(), entry.getValue());
        }
    }

    private void insertIntoItemAndEntryTables(int serialNumber, String entry) {
        try {
            String item = Jackson.newObjectMapper().readValue(entry, JsonNode.class).get("entry").toString();
            String sha256 = DigestUtils.sha256Hex(item);
            testDAO.testEntryDAO.insert(serialNumber, sha256);
            testDAO.testItemDAO.insertIfNotExist(sha256, item);
        } catch (Exception e) {
            Throwables.propagate(e);
        }
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
