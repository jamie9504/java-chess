package chess.controller;

import chess.domain.board.ChessBoard;
import chess.domain.piece.Color;
import chess.domain.GameState;
import chess.domain.board.Square;
import chess.view.InputView;
import chess.view.OutputView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChessController {

    public static void run() {
        OutputView.printStartGame();
        OutputView.printStartEndOption();
        ChessBoard chessBoard = new ChessBoard();

        start(chessBoard);
        String input;
        GameState gameState;
        boolean blackTurn = false;
        while (true) {
            input = InputView.inputStart();
            List<Square> squares = new ArrayList<>();
            if (input.length() == 10) {
                List<String> inputs = Arrays.asList(input.split(" "));
                input = inputs.get(0);
                squares.add(Square.of(inputs.get(1)));
                squares.add(Square.of(inputs.get(2)));
            }
            gameState = GameState.of(input);
            if (gameState == GameState.START) {
                throw new IllegalArgumentException("왜 시작하세요");
            }
            if (gameState == GameState.MOVE) {
                if (proceed(chessBoard, squares, blackTurn)) {
                    blackTurn = !blackTurn;
                }
            }
            if (chessBoard.isKingCaptured()) {
                if (blackTurn) {
                    OutputView.printWinner(Color.WHITE);
                    break;
                }
                OutputView.printWinner(Color.BLACK);
                break;
            }
            if (gameState == GameState.END) {
                printScoreAndWinners(chessBoard);
                break;
            }
            if (gameState == GameState.STATUS) {
                printScoreAndWinners(chessBoard);
            }
        }
    }

    private static void printScoreAndWinners(ChessBoard chessBoard) {
        OutputView.printScore(chessBoard.getTeamScore());
        OutputView.printWinners(chessBoard.getWinners());
    }

    private static boolean proceed(ChessBoard chessBoard, List<Square> squares, boolean blackTurn) {
        if (chessBoard.canMove(squares, blackTurn)) {
            chessBoard.movePiece(squares);
            OutputView.printChessBoard(chessBoard);
            return true;
        }
        OutputView.printCanNotMove();
        return false;
    }

    private static void start(ChessBoard chessBoard) {
        GameState gameState = GameState.of(InputView.inputStart());
        if (gameState != GameState.START) {
            throw new IllegalArgumentException("게임을 시작해야 합니다");
        }
        OutputView.printChessBoard(chessBoard);
    }
}
