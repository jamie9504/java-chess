package chess.domain.piece;

import chess.domain.board.Square;

import java.util.*;
import java.util.stream.Collectors;

public class Piece {
    private final static Map<String, Piece> CACHE = new HashMap<>();

    static {
        for (Color color : Color.values()) {
            for (Type type : Type.values()) {
                CACHE.put(color.getName() + type.getName(), new Piece(color, type));
            }
        }
    }

    private final Color color;
    private final Type type;

    private Piece(Color color, Type type) {
        this.color = color;
        this.type = type;
    }

    public static Piece of(Color color, Type type) {
        validateInput(color, type);
        return CACHE.get(color.getName() + type.getName());
    }

    private static void validateInput(Color color, Type type) {
        if (Objects.isNull(color) || Objects.isNull(type)) {
            throw new IllegalArgumentException("잘못된 입력입니다");
        }
    }

    public String getLetter() {
        if (color == Color.BLACK) {
            return type.getName();
        }
        return type.getName().toLowerCase();
    }

    public Set<Square> getCheatSheet(Square square) {
        Set<Square> availableSquares = new HashSet<>();

        if (type.equals(Type.QUEEN)) {
            for (int index = -7; index < 8; index++) {
                availableSquares.add(square.addIfInBoundary(index, 0));
                availableSquares.add(square.addIfInBoundary(0, index));
                availableSquares.add(square.addIfInBoundary(index * -1, index));
                availableSquares.add(square.addIfInBoundary(index, index));
            }
            availableSquares.remove(square);
            return availableSquares;
        }

        if (type.equals(Type.BISHOP)) {
            for (int index = -7; index < 8; index++) {
                availableSquares.add(square.addIfInBoundary(index * -1, index));
                availableSquares.add(square.addIfInBoundary(index, index));
            }
            availableSquares.remove(square);
            return availableSquares;
        }

        if (type.equals(Type.ROOK)) {
            for (int index = -7; index < 8; index++) {
                availableSquares.add(square.addIfInBoundary(index, 0));
                availableSquares.add(square.addIfInBoundary(0, index));
            }
            availableSquares.remove(square);
            return availableSquares;
        }

        if (type.equals(Type.KING)) {
            int index = -1;
            for (int i = 0; i < 2; i++) {
                availableSquares.add(square.addIfInBoundary(index, 0));
                availableSquares.add(square.addIfInBoundary(0, index));
                availableSquares.add(square.addIfInBoundary(index * -1, index));
                availableSquares.add(square.addIfInBoundary(index, index));
                index *= -1;
            }
            return availableSquares;
        }

        if (type.equals(Type.KNIGHT)) {
            int x = -1;
            int y = 2;
            for (int i = 0; i < 2; i++) {
                availableSquares.add(square.addIfInBoundary(x, y));
                availableSquares.add(square.addIfInBoundary(y, x));
                availableSquares.add(square.addIfInBoundary(x, (-1) * y));
                availableSquares.add(square.addIfInBoundary(y * -1, x));
                x *= -1;
                y *= -1;
            }
            return availableSquares;
        }

        if (type.equals(Type.PAWN)) {
            int index = 1;
            if (color.equals(Color.BLACK)) {
                index *= -1;
            }
            if ((color.equals(Color.BLACK) && square.getRank() == 7) ||
                    (color.equals(Color.WHITE) && square.getRank() == 2)) {
                availableSquares.add(square.addIfInBoundary(0, index * 2));
            }
            availableSquares.add(square.addIfInBoundary(0, index));
            return availableSquares;
        }

        throw new IllegalArgumentException("올바른 타입이 아닙니다");
    }

    public Set<Square> calculateMoveBoundary(Square square, Map<Square, Piece> board) {
        validateNotNull(square, board);

        if (type == Type.KNIGHT || type == Type.KING) {
            return getCheatSheet(square).stream()
                    .filter(s -> !(board.containsKey(s) && board.get(s).color == color))
                    .collect(Collectors.toSet());
        }

        if (type == Type.PAWN) {
            Set<Square> squares = getCheatSheet(square);
            for (Square s : squares) {
                if (Math.abs(square.getRank() - s.getRank()) == 1) {
                    Square squareRight = s;
                    Square squareLeft = s;
                    if (s.getFile() != 'a') {
                        squareLeft = Square.of(s, -1, 0);
                    }
                    if (s.getFile() != 'h') {
                        squareRight = Square.of(s, 1, 0);
                    }
                    if (board.containsKey(s) && color == board.get(s).color) {
                        squares.removeAll(getCheatSheet(square));
                    }
                    if (board.containsKey(squareRight) && color != board.get(squareRight).color) {
                        squares.add(squareRight);
                    }
                    if (board.containsKey(squareLeft) && color != board.get(squareLeft).color) {
                        squares.add(squareLeft);
                    }
                    continue;
                }
                if (board.containsKey(s) && color == board.get(s).color) {
                    squares.remove(s);
                }
            }
            return squares;
        }

        if (type == Type.QUEEN) {
            Set<Square> squares = getCheatSheet(square);
            Set<Square> squaresIter = getCheatSheet(square);
            for (Square s : squaresIter) {
                if (board.containsKey(s)) {
                    int fileDifference = s.getFile() - square.getFile();
                    int rankDifference = s.getRank() - square.getRank();
                    if (fileDifference == 0 && rankDifference > 0) {
                        Set<Square> squaresToRemove = findSquaresToRemove(s, 0, 1);
                        squares.removeAll(squaresToRemove);
                    }

                    if (fileDifference > 0 && rankDifference == 0) {
                        Set<Square> squaresToRemove = findSquaresToRemove(s, 1, 0);
                        squares.removeAll(squaresToRemove);
                    }

                    if (fileDifference < 0 && rankDifference == 0) {
                        Set<Square> squaresToRemove = findSquaresToRemove(s, -1, 0);
                        squares.removeAll(squaresToRemove);
                    }

                    if (fileDifference == 0 && rankDifference < 0) {
                        Set<Square> squaresToRemove = findSquaresToRemove(s, 0, -1);
                        squares.removeAll(squaresToRemove);
                    }

                    if (fileDifference > 0 && rankDifference > 0) {
                        Set<Square> squaresToRemove = findSquaresToRemove(s, 1, 1);
                        squares.removeAll(squaresToRemove);
                    }

                    if (fileDifference > 0 && rankDifference < 0) {
                        Set<Square> squaresToRemove = findSquaresToRemove(s, 1, -1);
                        squares.removeAll(squaresToRemove);
                    }

                    if (fileDifference < 0 && rankDifference > 0) {
                        Set<Square> squaresToRemove = findSquaresToRemove(s, -1, 1);
                        squares.removeAll(squaresToRemove);
                    }

                    if (fileDifference < 0 && rankDifference < 0) {
                        Set<Square> squaresToRemove = findSquaresToRemove(s, -1, -1);
                        squares.removeAll(squaresToRemove);
                    }

                    if (board.get(s).color == color) {
                        squares.remove(s);
                    }
                }
            }
            return squares;
        }

        if (type == Type.BISHOP) {
            Set<Square> squares = getCheatSheet(square);
            Set<Square> squaresIter = getCheatSheet(square);
            for (Square s : squaresIter) {
                if (board.containsKey(s)) {
                    int fileDifference = s.getFile() - square.getFile();
                    int rankDifference = s.getRank() - square.getRank();

                    if (fileDifference > 0 && rankDifference > 0) {
                        Set<Square> squaresToRemove = findSquaresToRemove(s, 1, 1);
                        squares.removeAll(squaresToRemove);
                    }

                    if (fileDifference > 0 && rankDifference < 0) {
                        Set<Square> squaresToRemove = findSquaresToRemove(s, 1, -1);
                        squares.removeAll(squaresToRemove);
                    }

                    if (fileDifference < 0 && rankDifference > 0) {
                        Set<Square> squaresToRemove = findSquaresToRemove(s, -1, 1);
                        squares.removeAll(squaresToRemove);
                    }

                    if (fileDifference < 0 && rankDifference < 0) {
                        Set<Square> squaresToRemove = findSquaresToRemove(s, -1, -1);
                        squares.removeAll(squaresToRemove);
                    }

                    if (board.get(s).color == color) {
                        squares.remove(s);
                    }
                }
            }
            return squares;
        }

        if (type == Type.ROOK) {
            Set<Square> squares = getCheatSheet(square);
            Set<Square> squaresIter = getCheatSheet(square);
            for (Square s : squaresIter) {
                if (board.containsKey(s)) {
                    int fileDifference = s.getFile() - square.getFile();
                    int rankDifference = s.getRank() - square.getRank();
                    if (fileDifference == 0 && rankDifference > 0) {
                        Set<Square> squaresToRemove = findSquaresToRemove(s, 0, 1);
                        squares.removeAll(squaresToRemove);
                    }

                    if (fileDifference > 0 && rankDifference == 0) {
                        Set<Square> squaresToRemove = findSquaresToRemove(s, 1, 0);
                        squares.removeAll(squaresToRemove);
                    }

                    if (fileDifference < 0 && rankDifference == 0) {
                        Set<Square> squaresToRemove = findSquaresToRemove(s, -1, 0);
                        squares.removeAll(squaresToRemove);
                    }

                    if (fileDifference == 0 && rankDifference < 0) {
                        Set<Square> squaresToRemove = findSquaresToRemove(s, 0, -1);
                        squares.removeAll(squaresToRemove);
                    }

                    if (board.get(s).color == color) {
                        squares.remove(s);
                    }
                }
            }
            return squares;
        }

        throw new IllegalArgumentException("no type");
    }

    private Set<Square> findSquaresToRemove(Square s, int fileAddAmount, int rankAddAmount) {
        Set<Square> squaresToRemove = new HashSet<>();
        int file = 0;
        int rank = 0;
        for (int i = 0; i < 8; i++, file += fileAddAmount, rank += rankAddAmount) {
            squaresToRemove.add(s.addIfInBoundary(file, rank));
        }
        squaresToRemove.remove(s);
        return squaresToRemove;
    }

    private void validateNotNull(Square square, Map<Square, Piece> board) {
        if (Objects.isNull(square) || Objects.isNull(board)) {
            throw new IllegalArgumentException("null 안댐");
        }
    }

    public boolean isBlack() {
        return color == Color.BLACK;
    }

    public double getScore() {
        return type.getScore();
    }
}