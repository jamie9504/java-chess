package chess.model.service;

import chess.model.domain.board.BoardInitialByDB;
import chess.model.domain.board.BoardInitialization;
import chess.model.domain.board.BoardSquare;
import chess.model.domain.board.CastlingSetting;
import chess.model.domain.board.ChessGame;
import chess.model.domain.board.EnPassant;
import chess.model.domain.board.TeamScore;
import chess.model.domain.piece.Color;
import chess.model.domain.piece.Piece;
import chess.model.domain.piece.Type;
import chess.model.domain.state.MoveOrder;
import chess.model.domain.state.MoveSquare;
import chess.model.domain.state.MoveState;
import chess.model.dto.ChessGameDto;
import chess.model.dto.MoveDto;
import chess.model.dto.PathDto;
import chess.model.dto.PromotionTypeDto;
import chess.model.dto.SourceDto;
import chess.model.repository.ChessBoardDao;
import chess.model.repository.ChessGameDao;
import chess.model.repository.ChessResultDao;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ChessGameService {

    private static final ChessGameDao CHESS_GAME_DAO = ChessGameDao.getInstance();
    private static final ChessBoardDao CHESS_BOARD_DAO = ChessBoardDao.getInstance();
    private static final ChessResultDao CHESS_RESULT_DAO = ChessResultDao.getInstance();
    private static final ChessGameService INSTANCE = new ChessGameService();

    private ChessGameService() {
    }

    public static ChessGameService getInstance() {
        return INSTANCE;
    }

    public int getIdBefore(int roomId, Map<Color, String> userNames) throws SQLException {
        Optional<Integer> gameNumberLatest = CHESS_GAME_DAO.getGameNumberLatest(roomId);
        if (gameNumberLatest.isPresent()) {
            return gameNumberLatest.get();
        }
        createChessGame(roomId, userNames);
        return CHESS_GAME_DAO.getGameNumberLatest(roomId).orElseThrow(IllegalAccessError::new);
    }

    public int createChessGame(int roomId, Map<Color, String> userNames)
        throws SQLException {
        CHESS_GAME_DAO.updateProceedNByRoomId(roomId);
        ChessGame chessGame = new ChessGame();
        int gameId = CHESS_GAME_DAO.insert(roomId, chessGame.getGameTurn(), userNames);
        CHESS_BOARD_DAO.insert(gameId, chessGame.getChessBoard(), chessGame.getCastlingElements());
        CHESS_RESULT_DAO.put(gameId, chessGame.getTeamScore());
        return gameId;
    }

    public ChessGameDto loadChessGame(int gameId) throws SQLException {
        return new ChessGameDto(getChessGame(gameId), CHESS_GAME_DAO.getUserNames(gameId));
    }

    private ChessGame getChessGame(int gameId) throws SQLException {
        BoardInitialization boardInitialByDB = new BoardInitialByDB(
            CHESS_BOARD_DAO.getBoard(gameId));
        Color gameTurn = CHESS_GAME_DAO.getGameTurn(gameId).orElseThrow(IllegalAccessError::new);
        Set<CastlingSetting> castlingElements = CHESS_BOARD_DAO.getCastlingElements(gameId);
        EnPassant enPassant = CHESS_BOARD_DAO.getEnpassantBoard(gameId);
        return new ChessGame(boardInitialByDB, gameTurn, castlingElements, enPassant);
    }

    public ChessGameDto move(MoveDto moveDTO) throws SQLException {
        MoveSquare moveSquare = new MoveSquare(moveDTO.getSource(), moveDTO.getTarget());
        int gameId = moveDTO.getGameId();
        EnPassant enPassant = CHESS_BOARD_DAO.getEnpassantBoard(gameId);
        ChessGame chessGame = getChessGame(gameId);
        boolean canCastling = chessGame.canCastling(moveSquare);
        boolean pawnSpecialMove = chessGame.isPawnSpecialMove(moveSquare);
        MoveState moveState = chessGame.movePieceWhenCanMove(moveSquare);
        Color gameTurn = CHESS_GAME_DAO.getGameTurn(gameId).orElseThrow(IllegalAccessError::new);
        if (moveState.isSucceed()) {
            CHESS_BOARD_DAO.deleteBoardSquare(gameId, moveSquare.get(MoveOrder.AFTER));
            CHESS_BOARD_DAO.copyBoardSquare(gameId, moveSquare);
            CHESS_BOARD_DAO.deleteBoardSquare(gameId, moveSquare.get(MoveOrder.BEFORE));
            if (pawnSpecialMove) {
                CHESS_BOARD_DAO.updateEnPassant(gameId, moveSquare);
            }
            if (enPassant.hasOtherEnpassant(moveSquare.get(MoveOrder.AFTER), gameTurn)) {
                CHESS_BOARD_DAO.deleteEnpassant(gameId, moveSquare.get(MoveOrder.AFTER));
            }
            if (canCastling) {
                MoveSquare moveSquareRook = CastlingSetting.getMoveCastlingRook(moveSquare);
                CHESS_BOARD_DAO.copyBoardSquare(gameId, moveSquareRook);
                CHESS_BOARD_DAO.deleteBoardSquare(gameId, moveSquareRook.get(MoveOrder.BEFORE));
            }
        }
        if (moveState == MoveState.SUCCESS) {
            CHESS_GAME_DAO.updateTurn(gameId, chessGame.getGameTurn());
        }
        CHESS_RESULT_DAO.put(gameId, chessGame.getTeamScore());
        ChessGameDto chessGameDTO = new ChessGameDto(getChessGame(gameId), moveState,
            new TeamScore(CHESS_RESULT_DAO.select(gameId)), CHESS_GAME_DAO.getUserNames(gameId));
        if (moveState == MoveState.KING_CAPTURED) {
            CHESS_BOARD_DAO.deleteBoardSquare(gameId, moveSquare.get(MoveOrder.AFTER));
            CHESS_BOARD_DAO.copyBoardSquare(gameId, moveSquare);
            CHESS_BOARD_DAO.deleteBoardSquare(gameId, moveSquare.get(MoveOrder.BEFORE));
            CHESS_GAME_DAO.updateProceedN(gameId);
            chessGameDTO.clearPiece();
        }
        return chessGameDTO;
    }

    public ChessGameDto promotion(PromotionTypeDto promotionTypeDTO) throws SQLException {
        Type type = Type.of(promotionTypeDTO.getPromotionType());
        int gameId = promotionTypeDTO.getGameId();
        ChessGame chessGame = getChessGame(gameId);
        BoardSquare finishPawnBoard = chessGame.getFinishPawnBoard();
        Piece hopePiece = chessGame.getHopePiece(type);
        MoveState moveState = chessGame.promotion(type);
        if (moveState == MoveState.SUCCESS_PROMOTION) {
            CHESS_BOARD_DAO.updatePromotion(gameId, finishPawnBoard, hopePiece);
            CHESS_GAME_DAO.updateTurn(gameId, chessGame.getGameTurn());
            CHESS_RESULT_DAO.put(gameId, chessGame.getTeamScore());
        }
        return new ChessGameDto(getChessGame(gameId), moveState,
            new TeamScore(CHESS_RESULT_DAO.select(gameId)), CHESS_GAME_DAO.getUserNames(gameId));
    }

    public PathDto getPath(SourceDto sourceDto) throws SQLException {
        ChessGame chessGame = getChessGame(sourceDto.getGameId());
        return new PathDto(chessGame.getCheatSheet(BoardSquare.of(sourceDto.getSource())));
    }

    public ChessGameDto endGame(MoveDto moveDto) throws SQLException {
        int gameId = moveDto.getGameId();
        CHESS_GAME_DAO.updateProceedN(gameId);
        ChessGameDto chessGameDto = new ChessGameDto(new TeamScore(CHESS_RESULT_DAO.select(gameId)),
            CHESS_GAME_DAO.getUserNames(gameId));
        chessGameDto.clearPiece();
        return chessGameDto;
    }
}
