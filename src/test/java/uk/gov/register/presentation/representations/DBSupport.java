package uk.gov.register.presentation.representations;

import org.postgresql.util.PGobject;
import uk.gov.register.presentation.functional.FunctionalTestBase;

import java.sql.*;
import java.util.List;

public class DBSupport {
    private static Connection createConnection() throws SQLException {
        org.apache.tomcat.jdbc.pool.DataSource dataSource = new org.apache.tomcat.jdbc.pool.DataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(FunctionalTestBase.DATABASE_URL);
        dataSource.setUsername(FunctionalTestBase.DATABASE_USER);
        return dataSource.getConnection();

    }


    public static void publishMessages(List<String> messages) {
        try (Connection connection = createConnection()) {
            for (String message : messages) {

                try (PreparedStatement insertPreparedStatement = connection.prepareStatement("Insert into ordered_entry_index(entry) values(?)")) {
                    insertPreparedStatement.setObject(1, jsonbObject(message));
                    insertPreparedStatement.execute();
                }

                Statement statement = connection.createStatement();

                String primaryKeyValue = extractRegisterKey(message);
                int id = getIdOfThisPublishedMessage(statement);

                if (isSupersedingAnEntry(statement, primaryKeyValue)) {
                    statement.executeUpdate(String.format("Update current_keys set id=%s where key='%s'", id, primaryKeyValue));
                } else {
                    statement.executeUpdate(String.format("Insert into current_keys(id,key) values(%s,%s)", id, primaryKeyValue));
                }

                statement.executeUpdate("Update register_entries_count set count=count+1");

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    private static boolean isSupersedingAnEntry(Statement statement, String primaryKeyValue) throws SQLException {
        statement.execute(String.format("select id from current_keys where key='%s'", primaryKeyValue));
        return statement.getResultSet().next();
    }

    private static String extractRegisterKey(String message) {
        return message.replaceAll(".*\"address\"\\s*:\\s*\"([^\"]+)\".*", "$1");
    }

    private static int getIdOfThisPublishedMessage(Statement statement) throws SQLException {
        statement.execute("select id from ordered_entry_index order by id desc limit 1");
        ResultSet resultSet = statement.getResultSet();
        resultSet.next();
        return resultSet.getInt("id");
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
