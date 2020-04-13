package chess.model.repository;

import chess.model.domain.board.BoardSquare;
import chess.model.domain.board.CastlingSetting;
import chess.model.domain.board.EnPassant;
import chess.model.domain.piece.Piece;
import chess.model.domain.piece.PieceFactory;
import chess.model.domain.state.MoveOrder;
import chess.model.domain.state.MoveSquare;
import chess.model.repository.template.JdbcTemplate;
import chess.model.repository.template.PreparedStatementSetter;
import chess.model.repository.template.ResultSetMapper;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ChessBoardDao {

    private final static ChessBoardDao INSTANCE = new ChessBoardDao();

    private ChessBoardDao() {
    }

    public static ChessBoardDao getInstance() {
        return INSTANCE;
    }

    public void insert(int gameId, Map<BoardSquare, Piece> board,
        Map<BoardSquare, Boolean> castlingElements) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        String query = "INSERT INTO CHESS_BOARD_TB(GAME_ID, BOARDSQUARE_NM, PIECE_NM, CASTLING_ELEMENT_YN) VALUES (?, ?, ?, ?)";
        PreparedStatementSetter pss = pstmt -> {
            for (BoardSquare boardSquare : board.keySet()) {
                pstmt.setInt(1, gameId);
                pstmt.setString(2, boardSquare.getName());
                pstmt.setString(3, PieceFactory.getName(board.get(boardSquare)));
                pstmt.setString(4, changeBooleanToString(castlingElements.get(boardSquare)));
                pstmt.executeUpdate();
            }
        };
        jdbcTemplate.executeUpdateWhenLoop(query, pss);
    }

    private String changeBooleanToString(boolean changer) {
        return changer ? "Y" : "N";
    }

    public void insertBoard(int gameId, BoardSquare boardSquare, Piece piece) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        String query = "INSERT INTO CHESS_BOARD_TB(GAME_ID, BOARDSQUARE_NM, PIECE_NM, CASTLING_ELEMENT_YN) VALUES (?, ?, ?, 'N')";
        PreparedStatementSetter pss = JdbcTemplate
            .getPssFromParams(gameId, boardSquare.getName(), PieceFactory.getName(piece));
        jdbcTemplate.executeUpdate(query, pss);
    }

    public void deleteBoardSquare(int gameId, BoardSquare boardSquare) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        String query = "DELETE FROM CHESS_BOARD_TB WHERE GAME_ID = ? AND BOARDSQUARE_NM = ?";
        PreparedStatementSetter pss = JdbcTemplate.getPssFromParams(gameId, boardSquare.getName());
        jdbcTemplate.executeUpdate(query, pss);
    }

    public Set<CastlingSetting> getCastlingElements(int gameId) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        String query = "SELECT BOARDSQUARE_NM, PIECE_NM FROM CHESS_BOARD_TB WHERE GAME_ID = ? AND CASTLING_ELEMENT_YN = 'Y'";
        PreparedStatementSetter pss = pstmt -> pstmt.setInt(1, gameId);
        ResultSetMapper<Set<CastlingSetting>> mapper = rs -> {
            Set<CastlingSetting> castlingElements = new HashSet<>();
            while (rs.next()) {
                castlingElements
                    .add(CastlingSetting.of(BoardSquare.of(rs.getString("BOARDSQUARE_NM")),
                        PieceFactory.of(rs.getString("PIECE_NM"))));
            }
            return castlingElements;
        };
        return jdbcTemplate.executeQuery(query, pss, mapper);
    }

    public EnPassant getEnpassantBoard(int gameId) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        String query = "SELECT EN_PASSANT_NM, BOARDSQUARE_NM FROM CHESS_BOARD_TB WHERE GAME_ID = ? AND EN_PASSANT_NM IS NOT NULL";
        PreparedStatementSetter pss = pstmt -> pstmt.setInt(1, gameId);
        ResultSetMapper<EnPassant> mapper = rs -> {
            Map<BoardSquare, BoardSquare> board = new HashMap<>();
            while (rs.next()) {
                board.put(BoardSquare.of(rs.getString("EN_PASSANT_NM")),
                    BoardSquare.of(rs.getString("BOARDSQUARE_NM")));
            }
            return new EnPassant(board);
        };
        return jdbcTemplate.executeQuery(query, pss, mapper);
    }

    public Map<BoardSquare, Piece> getBoard(int gameId) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        String query = "SELECT BOARDSQUARE_NM, PIECE_NM FROM CHESS_BOARD_TB WHERE GAME_ID = ?";
        PreparedStatementSetter pss = pstmt -> pstmt.setInt(1, gameId);
        ResultSetMapper<Map<BoardSquare, Piece>> mapper = rs -> {
            Map<BoardSquare, Piece> board = new HashMap<>();
            while (rs.next()) {
                board.put(BoardSquare.of(rs.getString("BOARDSQUARE_NM")),
                    PieceFactory.of(rs.getString("PIECE_NM")));
            }
            return board;
        };
        return jdbcTemplate.executeQuery(query, pss, mapper);
    }

    public void delete(int gameId) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        String query = "DELETE FROM CHESS_BOARD_TB WHERE GAME_ID = ?";
        PreparedStatementSetter pss = pstmt -> pstmt.setInt(1, gameId);
        jdbcTemplate.executeUpdate(query, pss);
    }

    public void copyBoardSquare(int gameId, MoveSquare moveSquare) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        StringBuilder query = new StringBuilder();
        query.append("INSERT ");
        query
            .append("INTO CHESS_BOARD_TB(GAME_ID, BOARDSQUARE_NM, PIECE_NM, CASTLING_ELEMENT_YN) ");
        query.append("VALUES (?, ?, ( ");
        query.append("SELECT PIECE_NM ");
        query.append("FROM CHESS_BOARD_TB AS BOARD ");
        query.append("JOIN CHESS_GAME_TB  AS GAME ");
        query.append("WHERE BOARD.GAME_ID = GAME.ID ");
        query.append("AND GAME.PROCEEDING_YN = 'Y' ");
        query.append("AND GAME.ID = ? ");
        query.append("AND BOARDSQUARE_NM = ?), 'N')");

        PreparedStatementSetter pss = JdbcTemplate
            .getPssFromParams(gameId, moveSquare.get(MoveOrder.AFTER).getName(), gameId,
                moveSquare.get(MoveOrder.BEFORE).getName());
        jdbcTemplate.executeUpdate(query.toString(), pss);
    }

    public void updatePromotion(int gameId, BoardSquare finishPawnBoard, Piece hopePiece) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        String query = "UPDATE CHESS_BOARD_TB SET PIECE_NM = ? WHERE GAME_ID = ? AND BOARDSQUARE_NM = ?";
        PreparedStatementSetter pss = JdbcTemplate
            .getPssFromParams(PieceFactory.getName(hopePiece), gameId, finishPawnBoard.getName());
        jdbcTemplate.executeUpdate(query, pss);
    }

    public void deleteEnpassant(int gameId, BoardSquare enpassantSquare) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        String query = "DELETE FROM CHESS_BOARD_TB WHERE GAME_ID = ? AND EN_PASSANT_NM = ?";
        PreparedStatementSetter pss = JdbcTemplate
            .getPssFromParams(gameId, enpassantSquare.getName());
        jdbcTemplate.executeUpdate(query, pss);
    }

    public void updateEnPassant(int gameId, MoveSquare moveSquare) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        String query = "UPDATE CHESS_BOARD_TB SET EN_PASSANT_NM = ? WHERE GAME_ID = ? AND BOARDSQUARE_NM = ?";
        PreparedStatementSetter pss = JdbcTemplate
            .getPssFromParams(moveSquare.getBetweenWhenJumpRank().getName(), gameId,
                moveSquare.get(MoveOrder.AFTER).getName());
        jdbcTemplate.executeUpdate(query, pss);
    }
}
