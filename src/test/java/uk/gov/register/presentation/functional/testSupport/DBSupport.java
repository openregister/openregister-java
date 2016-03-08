package uk.gov.register.presentation.functional.testSupport;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.postgresql.util.PGobject;
import uk.gov.register.presentation.functional.FunctionalTestBase;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DBSupport {

    private static final DataSource pooledDataSource = new DataSource();

    static {
        pooledDataSource.setDriverClassName("org.postgresql.Driver");
        pooledDataSource.setUrl(FunctionalTestBase.DATABASE_URL);
        pooledDataSource.setUsername(FunctionalTestBase.DATABASE_USER);
    }

    public static void main(String[] args) throws IOException, SQLException {
        String filePath = args[1];
        DataSource dataSource = new DataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(args[2]);
        dataSource.setUsername("postgres");

        try (Stream<String> lines = Files.lines(new File(filePath).toPath(), Charset.defaultCharset()); Connection connection = dataSource.getConnection()) {
            List<String> collect = lines.collect(Collectors.toList());
            publishMessages(connection, args[0], collect);
        }
    }

    public static void publishMessages(List<String> messages) {
        publishMessages("address", messages);
    }

    public static void publishMessages(String registerName, List<String> messages) {
        SortedMap<Integer, String> messagesWithSerialNumbers = messages.stream().collect(Collectors.toMap(m -> messages.indexOf(m) + 1, m -> m, (a, b) -> a, TreeMap::new));
        publishMessages(registerName, messagesWithSerialNumbers);
    }

    public static void publishMessages(String registerName, SortedMap<Integer, String> messages) {
        try (Connection connection = pooledDataSource.getConnection()) {
            publishMessages(connection, registerName, messages);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void publishMessages(Connection connection, String registerName, List<String> messages) throws SQLException {
        SortedMap<Integer, String> messagesWithSerialNumbers = messages.stream().collect(Collectors.toMap(m -> messages.indexOf(m) + 1, m -> m, (a, b) -> a, TreeMap::new));
        publishMessages(connection, registerName, messagesWithSerialNumbers);
    }

    private static void publishMessages(Connection connection, String registerName, SortedMap<Integer, String> messages) throws SQLException {
        for (SortedMap.Entry<Integer, String> entry : messages.entrySet()) {
            int serialNumber = entry.getKey();
            String message = entry.getValue();
            try (PreparedStatement insertPreparedStatement = connection.prepareStatement("Insert into ordered_entry_index(serial_number,entry) values(?,?)")) {
                insertPreparedStatement.setObject(1, serialNumber);
                insertPreparedStatement.setObject(2, jsonbObject(message));
                insertPreparedStatement.execute();
            }

            updateOtherTables(connection, registerName, serialNumber, message);
        }
    }

    private static void updateOtherTables(Connection connection, String registerName, int serialNumber, String message) throws SQLException {
        try (Statement statement = connection.createStatement()) {

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
