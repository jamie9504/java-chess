package chess.model.repository.template;

import static chess.model.repository.connector.ChessMySqlConnector.getConnection;

import chess.model.repository.exception.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JdbcTemplate {

    public void executeUpdate(String query, PreparedStatementSetter pss) {
        try (Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(query)) {
            pss.setParameter(pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException();
        }
    }

    public void executeUpdate(String query, Object... params) {
        PreparedStatementSetter pss = getPssFromParams(params);
        executeUpdate(query, pss);
    }

    private PreparedStatementSetter getPssFromParams(Object[] params) {
        return pstmt -> {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
        };
    }

    public void executeUpdateWhenLoop(String query, PreparedStatementSetter loopPss) {
        try (Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(query)) {
            loopPss.setParameter(pstmt);
        } catch (SQLException e) {
            throw new DataAccessException();
        }
    }

    public int executeUpdateWithGeneratedKey(String query, PreparedStatementSetter pss) {
        try (Connection conn = getConnection();
            PreparedStatement pstmt =
                conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pss.setParameter(pstmt);
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                throw new DataAccessException();
            }
        } catch (SQLException e) {
            throw new DataAccessException();
        }
    }

    public int executeUpdateWithGeneratedKey(String query, Object... params) {
        PreparedStatementSetter pss = getPssFromParams(params);
        return executeUpdateWithGeneratedKey(query, pss);
    }

    public <T> T executeQuery(String query, ResultSetMapper<T> mapper,
        PreparedStatementSetter pss) {
        try (Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(query)) {
            pss.setParameter(pstmt);
            try (ResultSet rs = pstmt.executeQuery()) {
                return mapper.setRow(rs);
            }
        } catch (SQLException e) {
            throw new DataAccessException();
        }
    }

    public <T> T executeQuery(String query, ResultSetMapper<T> mapper, Object... params) {
        PreparedStatementSetter pss = getPssFromParams(params);
        return executeQuery(query, mapper, pss);
    }
}
