package org.simpleframework.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class JdbcTemplate {

    public static <T> T queryForObject(String sql, Function<ResultSet, T> mapper, Object... params) {
        try (
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement stmt = createPreparedStatement(connection, sql, params);
            ResultSet resultSet = stmt.executeQuery()
        ) {
            if (resultSet.next()) {
                return mapper.apply(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static <T> List<T> queryForList(String sql, Function<ResultSet, T> mapper, Object... params) {
        List<T> results = new ArrayList<>();
        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement stmt = createPreparedStatement(connection, sql, params);
                ResultSet resultSet = stmt.executeQuery()
        ) {
            while (resultSet.next()) {
                results.add(mapper.apply(resultSet));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    public static int update(String sql, Object... params) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = createPreparedStatement(connection, sql, params)) {
            return stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static void batchUpdate(String sql, List<Object[]> params) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)
        ) {
            for (Object[] param : params) {
                setParameters(stmt, param);
                stmt.addBatch();
            }

            stmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static PreparedStatement createPreparedStatement(Connection connection, String sql, Object... params) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(sql);

        return setParameters(stmt, params);
    }

    private static PreparedStatement setParameters(PreparedStatement stmt, Object[] params) throws SQLException {

        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }

        return stmt;
    }

}
