package chess.domain.piece;

import chess.domain.board.BoardSquare;
import util.NullChecker;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Queen extends Piece {
    private final static Map<Color, Piece> CACHE = new HashMap<>();

    private final static Type type = Type.QUEEN;

    static {
        for (Color color : Color.values()) {
            CACHE.put(color, new Queen(color, type));
        }
    }

    public Queen(Color color, Type type) {
        super(color, type);
    }

    public static Piece getPieceInstance(Color color) {
        NullChecker.validateNotNull(color);
        return CACHE.get(color);
    }

    @Override
    public Set<BoardSquare> getAllCheatSheet(BoardSquare boardSquare) {
        NullChecker.validateNotNull(boardSquare);
        Set<BoardSquare> availableBoardSquares = new HashSet<>();
        for (int index = -7; index < 8; index++) {
            availableBoardSquares.add(boardSquare.addIfInBoundary(index, 0));
            availableBoardSquares.add(boardSquare.addIfInBoundary(0, index));
            availableBoardSquares.add(boardSquare.addIfInBoundary(index * -1, index));
            availableBoardSquares.add(boardSquare.addIfInBoundary(index, index));
        }
        availableBoardSquares.remove(boardSquare);
        return availableBoardSquares;
    }

    @Override
    public Set<BoardSquare> getCheatSheet(BoardSquare boardSquare, Map<BoardSquare, Piece> board) {
        NullChecker.validateNotNull(boardSquare, board);
        Set<BoardSquare> boardSquares = getAllCheatSheet(boardSquare);
        Set<BoardSquare> squaresIter = getAllCheatSheet(boardSquare);
        for (BoardSquare s : squaresIter) {
            if (board.containsKey(s)) {
                int fileDifference = s.getFileSubtract(boardSquare);
                int rankDifference = s.getRankSubtract(boardSquare);
                if (fileDifference == 0 && rankDifference > 0) {
                    Set<BoardSquare> squaresToRemove = findSquaresToRemove(s, 0, 1);
                    boardSquares.removeAll(squaresToRemove);
                }

                if (fileDifference > 0 && rankDifference == 0) {
                    Set<BoardSquare> squaresToRemove = findSquaresToRemove(s, 1, 0);
                    boardSquares.removeAll(squaresToRemove);
                }

                if (fileDifference < 0 && rankDifference == 0) {
                    Set<BoardSquare> squaresToRemove = findSquaresToRemove(s, -1, 0);
                    boardSquares.removeAll(squaresToRemove);
                }

                if (fileDifference == 0 && rankDifference < 0) {
                    Set<BoardSquare> squaresToRemove = findSquaresToRemove(s, 0, -1);
                    boardSquares.removeAll(squaresToRemove);
                }

                if (fileDifference > 0 && rankDifference > 0) {
                    Set<BoardSquare> squaresToRemove = findSquaresToRemove(s, 1, 1);
                    boardSquares.removeAll(squaresToRemove);
                }

                if (fileDifference > 0 && rankDifference < 0) {
                    Set<BoardSquare> squaresToRemove = findSquaresToRemove(s, 1, -1);
                    boardSquares.removeAll(squaresToRemove);
                }

                if (fileDifference < 0 && rankDifference > 0) {
                    Set<BoardSquare> squaresToRemove = findSquaresToRemove(s, -1, 1);
                    boardSquares.removeAll(squaresToRemove);
                }

                if (fileDifference < 0 && rankDifference < 0) {
                    Set<BoardSquare> squaresToRemove = findSquaresToRemove(s, -1, -1);
                    boardSquares.removeAll(squaresToRemove);
                }

                if (isSameColor(board.get(s))) {
                    boardSquares.remove(s);
                }
            }
        }
        return boardSquares;

    }
}
