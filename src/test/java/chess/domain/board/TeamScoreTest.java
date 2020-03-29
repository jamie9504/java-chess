package chess.domain.board;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import chess.domain.piece.Color;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TeamScoreTest {

    @Test
    @DisplayName("Null이 생성자에 들어갔을 때 예외 발생")
    void validNotNull() {
        assertThatThrownBy(() -> new TeamScore(null, null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Null");
    }

    @Test
    @DisplayName("게임 점수 계산")
    void calculateScore() {
        ChessBoard chessBoard = new ChessBoard();
        TeamScore teamScore = chessBoard.getTeamScore();
        Map<Color, Double> teamScores = teamScore.getTeamScore();
        assertThat(teamScores.get(Color.BLACK)).isEqualTo(38);
        assertThat(teamScores.get(Color.WHITE)).isEqualTo(38);

        chessBoard.movePieceWhenCanMove(Arrays.asList(BoardSquare.of("c2"), BoardSquare.of("c4")));
        chessBoard.movePieceWhenCanMove(Arrays.asList(BoardSquare.of("d7"), BoardSquare.of("d5")));
        chessBoard.movePieceWhenCanMove(Arrays.asList(BoardSquare.of("c4"), BoardSquare.of("d5")));

        teamScore = chessBoard.getTeamScore();
        teamScores = teamScore.getTeamScore();
        assertThat(teamScores.get(Color.BLACK)).isEqualTo(37);
        assertThat(teamScores.get(Color.WHITE)).isEqualTo(37);
    }

    @Test
    @DisplayName("승자 구하기")
    void getWinnerByScore() {
        ChessBoard chessBoard = new ChessBoard();
        TeamScore teamScore = chessBoard.getTeamScore();
        assertThat(teamScore.getWinners().size()).isEqualTo(2);

        chessBoard.movePieceWhenCanMove(Arrays.asList(BoardSquare.of("b1"), BoardSquare.of("c3")));
        chessBoard.movePieceWhenCanMove(Arrays.asList(BoardSquare.of("d7"), BoardSquare.of("d5")));
        chessBoard.movePieceWhenCanMove(Arrays.asList(BoardSquare.of("c3"), BoardSquare.of("d5")));
        teamScore = chessBoard.getTeamScore();
        assertThat(teamScore.getWinners().size()).isEqualTo(1);
        assertThat(teamScore.getWinners().get(0)).isEqualTo(Color.WHITE);
    }
}
