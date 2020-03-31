package chess.domain.piece;

import chess.domain.board.BoardSquare;
import chess.domain.board.CastlingSetting;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import util.NullChecker;

public class Knight extends OneTimeMovePiece {

    private final static Map<Color, Piece> CACHE = new HashMap<>();

    static {
        for (Color color : Color.values()) {
            CACHE.put(color, new Knight(color, Type.KNIGHT));
        }
    }

    public Knight(Color color, Type type) {
        super(color, type);
    }

    public static Piece getPieceInstance(Color color) {
        NullChecker.validateNotNull(color);
        return CACHE.get(color);
    }

    @Override
    public Set<BoardSquare> getCheatSheet(BoardSquare boardSquare, Map<BoardSquare, Piece> board,
        Set<CastlingSetting> castlingElements) {
        return getAllCheatSheet(boardSquare).stream()
            .filter(s -> !(board.containsKey(s) && isSameColor(board.get(s))))
            .collect(Collectors.toSet());
    }
}
