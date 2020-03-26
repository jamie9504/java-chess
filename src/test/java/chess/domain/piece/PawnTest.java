package chess.domain.piece;

import chess.domain.board.Square;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class PawnTest {

    @Test
    @DisplayName("말의 위치(pawn)를 받고 말의 종류에 따라 이동할 수 있는 칸 리스트 반환")
    void calculateScopePawnBlack() {
        Piece pieceBlack = Pawn.getPieceInstance(Color.BLACK);
        Piece pieceWhite = Pawn.getPieceInstance(Color.WHITE);

        Set<Square> availableSquaresBlack = pieceBlack.getAllCheatSheet(Square.of("a7"));
        Set<Square> availableSquaresWhite = pieceWhite.getAllCheatSheet(Square.of("a6"));

        assertThat(availableSquaresBlack.contains(Square.of("a6"))).isTrue();
        assertThat(availableSquaresBlack.contains(Square.of("a5"))).isTrue();
        assertThat(availableSquaresWhite.contains(Square.of("a7"))).isTrue();

        assertThat(availableSquaresBlack.size()).isEqualTo(2);
        assertThat(availableSquaresWhite.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("판의 정보를 가져와서 폰이 갈 수 있는 칸에 장애물이 있는지 판단하여 이동할 수 있는 리스트 반환하는 테스트")
    void movablePawnSquareTest() {
        Map<Square, Piece> board = new HashMap<>();

        board.put(Square.of("b5"), Knight.getPieceInstance(Color.BLACK));
        board.put(Square.of("e5"), Knight.getPieceInstance(Color.BLACK));
        board.put(Square.of("f5"), Knight.getPieceInstance(Color.WHITE));

        Piece piece = Pawn.getPieceInstance(Color.BLACK);
        Set<Square> availableSquares = piece.calculateMoveBoundary(Square.of("c6"), board);
        assertThat(availableSquares.contains(Square.of("c5"))).isTrue();
        assertThat(availableSquares.size()).isEqualTo(1);

        availableSquares = piece.calculateMoveBoundary(Square.of("e6"), board);
        assertThat(availableSquares.contains(Square.of("f5"))).isTrue();
        assertThat(availableSquares.size()).isEqualTo(1);

        availableSquares = piece.calculateMoveBoundary(Square.of("g6"), board);
        assertThat(availableSquares.contains(Square.of("g5"))).isTrue();
        assertThat(availableSquares.contains(Square.of("f5"))).isTrue();
        assertThat(availableSquares.size()).isEqualTo(2);
    }

}
