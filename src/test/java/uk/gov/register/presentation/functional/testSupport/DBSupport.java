package uk.gov.register.presentation.functional.testSupport;

import com.fasterxml.jackson.databind.JsonNode;
import io.dropwizard.jackson.Jackson;
import org.postgresql.util.PGobject;
import uk.gov.register.presentation.functional.FunctionalTestBase;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DBSupport {
    public static void main(String[] args) throws IOException, SQLException {
        String filePath = args[1];
        try (Stream<String> lines = Files.lines(new File(filePath).toPath(), Charset.defaultCharset()); Connection connection = createConnection(args[2], "postgres")) {
            List<String> collect = lines.collect(Collectors.toList());
            publishMessages(connection, args[0], collect);
        }
    }

    private static Connection createConnection(String databaseUrl, String databaseUser) throws SQLException {
        org.apache.tomcat.jdbc.pool.DataSource dataSource = new org.apache.tomcat.jdbc.pool.DataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(databaseUrl);
        dataSource.setUsername(databaseUser);
        return dataSource.getConnection();

    }

    public static void publishMessages(List<String> messages) {
        publishMessages("address", messages);
    }

    public static  void publishMessages(String registerName, List<String> messages) {
        Map<Integer, String> messagesWithSerialNumbers = messages.stream().collect(Collectors.toMap(m -> messages.indexOf(m) + 1, m -> m));
        publishMessages(registerName, messagesWithSerialNumbers);
    }

    public static void publishMessages(String registerName, Map<Integer, String> messages) {
        try (Connection connection = createConnection(FunctionalTestBase.DATABASE_URL, FunctionalTestBase.DATABASE_USER)) {
            publishMessages(connection, registerName, messages);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeSignedTreeHead(int treeSize, long timeStamp, String rootHash, String treeSignature) {
        try (Connection connection = createConnection(FunctionalTestBase.DATABASE_URL, FunctionalTestBase.DATABASE_USER); Statement statement = connection.createStatement()) {
            statement.executeUpdate("Delete from sth");
            try (PreparedStatement preparedStatement = connection.prepareStatement("Insert into sth(tree_size,timestamp,tree_head_signature,sha256_root_hash) values (?,?,?,?)")) {
                preparedStatement.setInt(1, treeSize);
                preparedStatement.setLong(2, timeStamp);
                preparedStatement.setString(3, treeSignature);
                preparedStatement.setString(4, rootHash);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void publishMessages(Connection connection, String registerName, List<String> messages) throws SQLException {
        Map<Integer, String> messagesWithSerialNumbers = messages.stream().collect(Collectors.toMap(m -> messages.indexOf(m) + 1, m -> m));
        publishMessages(connection, registerName, messagesWithSerialNumbers);
    }


    private static void publishMessages(Connection connection, String registerName, Map<Integer, String> messages) throws SQLException {
        for (Map.Entry<Integer, String> entry : messages.entrySet()) {
            int serialNumber = entry.getKey();
            String message = entry.getValue();
            try (PreparedStatement insertPreparedStatement = connection.prepareStatement("Insert into ordered_entry_index(serial_number,entry,leaf_input) values(?,?, ?)")) {
                insertPreparedStatement.setObject(1, serialNumber);
                insertPreparedStatement.setObject(2, jsonbObject(message));
                insertPreparedStatement.setString(3, CTLeafInputGenerator.createLeafInputFrom(itemData(message), System.currentTimeMillis()));
                insertPreparedStatement.execute();
            }

            Statement statement = connection.createStatement();

            String primaryKeyValue = extractRegisterKey(registerName, message);

            if (isSupersedingAnEntry(statement, primaryKeyValue)) {
                statement.executeUpdate(String.format("Update current_keys set serial_number=%s where key='%s'", serialNumber, primaryKeyValue));
            } else {
                statement.executeUpdate(String.format("Insert into current_keys(serial_number,key) values(%s,'%s')", serialNumber, primaryKeyValue));
                statement.executeUpdate("Update total_records set count=count+1");
            }

            statement.executeUpdate("Update total_entries set count=count+1");
        }
    }

    private static boolean isSupersedingAnEntry(Statement statement, String primaryKeyValue) throws SQLException {
        statement.execute(String.format("select serial_number from current_keys where key='%s'", primaryKeyValue));
        return statement.getResultSet().next();
    }

    private static String itemData(String message) {
        try {
            return Jackson.newObjectMapper().readValue(message, JsonNode.class).get("entry").toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String extractRegisterKey(String registerName, String message) {
        return message.replaceAll(".*\"" + registerName + "\"\\s*:\\s*\"([^\"]+)\".*", "$1");
    }

    public static PGobject jsonbObject(String value) {
        try {
            PGobject pGobject = new PGobject();
            pGobject.setType("jsonb");
            pGobject.setValue(value);
            return pGobject;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
