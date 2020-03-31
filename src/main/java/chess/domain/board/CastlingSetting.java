package chess.domain.board;

import chess.domain.piece.Bishop;
import chess.domain.piece.Color;
import chess.domain.piece.King;
import chess.domain.piece.Knight;
import chess.domain.piece.Piece;
import chess.domain.piece.Queen;
import chess.domain.piece.Rook;
import chess.domain.state.MoveSquare;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public enum CastlingSetting {
    WHITE_ROOK_LEFT(BoardSquare.of("a1"), Rook.getPieceInstance(Color.WHITE), true),
    WHITE_ROOK_RIGHT(BoardSquare.of("h1"), Rook.getPieceInstance(Color.WHITE), true),
    BLACK_ROOK_LEFT(BoardSquare.of("h8"), Rook.getPieceInstance(Color.BLACK), true),
    BLACK_ROOK_RIGHT(BoardSquare.of("a8"), Rook.getPieceInstance(Color.BLACK), true),
    WHITE_KNIGHT_RIGHT(BoardSquare.of("g1"), Knight.getPieceInstance(Color.WHITE), false),
    BLACK_KNIGHT_LEFT(BoardSquare.of("g8"), Knight.getPieceInstance(Color.BLACK), false),
    WHITE_BISHOP_LEFT(BoardSquare.of("c1"), Bishop.getPieceInstance(Color.WHITE), false),
    WHITE_BISHOP_RIGHT(BoardSquare.of("f1"), Bishop.getPieceInstance(Color.WHITE), false),
    BLACK_BISHOP_LEFT(BoardSquare.of("f8"), Bishop.getPieceInstance(Color.BLACK), false),
    BLACK_BISHOP_RIGHT(BoardSquare.of("c8"), Bishop.getPieceInstance(Color.BLACK), false),
    WHITE_QUEEN(BoardSquare.of("d1"), Queen.getPieceInstance(Color.WHITE), false),
    BLACK_QUEEN(BoardSquare.of("d8"), Queen.getPieceInstance(Color.BLACK), false),
    WHITE_KING(BoardSquare.of("e1"), King.getPieceInstance(Color.WHITE), true),
    BLACK_KING(BoardSquare.of("e8"), King.getPieceInstance(Color.BLACK), true);

    private final BoardSquare boardSquare;
    private final Piece piece;
    private final boolean CastlingPiece;

    CastlingSetting(BoardSquare boardSquare, Piece piece, boolean castlingPiece) {
        this.boardSquare = boardSquare;
        this.piece = piece;
        CastlingPiece = castlingPiece;
    }

    public static MoveSquare getMoveCastlingRook(BoardSquare moveSquareAfter) {
        if (moveSquareAfter == WHITE_BISHOP_LEFT.boardSquare) {
            return new MoveSquare(WHITE_ROOK_LEFT.boardSquare, WHITE_QUEEN.boardSquare);
        }
        if (moveSquareAfter == WHITE_KNIGHT_RIGHT.boardSquare) {
            return new MoveSquare(WHITE_ROOK_RIGHT.boardSquare, WHITE_BISHOP_RIGHT.boardSquare);
        }
        if (moveSquareAfter == BLACK_KNIGHT_LEFT.boardSquare) {
            return new MoveSquare(BLACK_ROOK_LEFT.boardSquare, WHITE_BISHOP_LEFT.boardSquare);
        }
        if (moveSquareAfter == BLACK_BISHOP_RIGHT.boardSquare) {
            return new MoveSquare(BLACK_ROOK_RIGHT.boardSquare, BLACK_QUEEN.boardSquare);
        }
        throw new IllegalArgumentException("잘못된 인자");
    }

    public Piece getPiece() {
        return piece;
    }

    public static Set<CastlingSetting> getCastlingElements() {
        return Arrays.stream(CastlingSetting.values())
            .filter(castlingElement -> castlingElement.CastlingPiece)
            .collect(Collectors.toSet());
    }

    public boolean isContains(BoardSquare boardSquare) {
        return this.boardSquare == boardSquare;
    }

    public boolean isSameColor(Piece piece) {
        return this.piece.isSameColor(piece);
    }

    public static Set<BoardSquare> getCastlingCheatSheets(
        Set<CastlingSetting> castlingElements) {
        Set<BoardSquare> castlingCheatSheets = new HashSet<>();
        if (castlingElements.contains(WHITE_ROOK_LEFT) && castlingElements.contains(WHITE_KING)) {
            castlingCheatSheets.add(WHITE_BISHOP_LEFT.boardSquare);
        }
        if (castlingElements.contains(WHITE_ROOK_RIGHT) && castlingElements.contains(WHITE_KING)) {
            castlingCheatSheets.add(WHITE_KNIGHT_RIGHT.boardSquare);
        }
        if (castlingElements.contains(BLACK_ROOK_LEFT) && castlingElements.contains(BLACK_KING)) {
            castlingCheatSheets.add(BLACK_KNIGHT_LEFT.boardSquare);
        }
        if (castlingElements.contains(BLACK_ROOK_RIGHT) && castlingElements.contains(BLACK_KING)) {
            castlingCheatSheets.add(BLACK_BISHOP_RIGHT.boardSquare);
        }
        return Collections.unmodifiableSet(castlingCheatSheets);
    }

    public boolean isSameSquare(BoardSquare moveSquare) {
        return this.boardSquare == moveSquare;
    }
}
