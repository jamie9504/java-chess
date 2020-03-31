package chess.domain.piece;

import chess.domain.board.BoardSquare;
import chess.domain.board.CastlingSetting;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import util.NullChecker;

public class King extends OneTimeMovePiece {

    private final static Map<Color, Piece> CACHE = new HashMap<>();

    static {
        for (Color color : Color.values()) {
            CACHE.put(color, new King(color, Type.KING));
        }
    }

    public King(Color color, Type type) {
        super(color, type);
    }

    public static Piece getPieceInstance(Color color) {
        NullChecker.validateNotNull(color);
        return CACHE.get(color);
    }

    @Override
    public Set<BoardSquare> getCheatSheet(BoardSquare boardSquare, Map<BoardSquare, Piece> board,
        Set<CastlingSetting> castlingElements) {
        Set<BoardSquare> cheatSheet = getAllCheatSheet(boardSquare).stream()
            .filter(s -> !(board.containsKey(s) && isSameColor(board.get(s))))
            .collect(Collectors.toSet());
        cheatSheet.addAll(getCastlingCheatSheet(boardSquare, board, castlingElements));
        return cheatSheet;
    }

    private Set<BoardSquare> getCastlingCheatSheet(BoardSquare boardSquare,
        Map<BoardSquare, Piece> board, Set<CastlingSetting> castlingElements) {
        Set<CastlingSetting> sameColorCastlingElements = castlingElements.stream()
            .filter(castlingElement -> castlingElement.isSameColor(this))
            .collect(Collectors.toSet());

        Set<BoardSquare> castlingCheatSheets = CastlingSetting
            .getCastlingCheatSheets(sameColorCastlingElements);
        Set<BoardSquare> totalCheatSheet = new HashSet<>();

        for (BoardSquare castlingCheatSheet : castlingCheatSheets) {
            Set<BoardSquare> boardSquaresForCastling = new HashSet<>();
            int fileCompare = boardSquare.getFileCompare(castlingCheatSheet);
            for (int i = 1; i < BoardSquare.MAX_FILE_AND_RANK_COUNT; i++) {
                if (boardSquare.hasIncreased(fileCompare * -i, 0)) {
                    boardSquaresForCastling
                        .add(boardSquare.getIncreased(fileCompare * -i, 0));
                }
            }
            int nonContains = (int) boardSquaresForCastling.stream()
                .filter(square -> !board.containsKey(square))
                .count() + 1;
            if (boardSquaresForCastling.size() == nonContains) {
                totalCheatSheet.add(castlingCheatSheet);
            }
        }
        return totalCheatSheet;
    }
}
