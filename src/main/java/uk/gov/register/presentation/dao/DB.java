package uk.gov.register.presentation.dao;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DB {
    private final DataSource dataSource;

    public DB(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    <T> T select(String query, ResultCollector<T> resultCollector, Object... params) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                for (int i = 1; i <= params.length; i++) {
                    preparedStatement.setObject(i, params[i - 1]);
                }
                ResultSet resultSet = preparedStatement.executeQuery();
                return resultCollector.collect(resultSet);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public interface ResultCollector<T> {
        T collect(ResultSet resultSet) throws SQLException;
    }
}
