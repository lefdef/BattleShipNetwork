package edu.cs4962.example.battleshipnetwork;

import java.util.List;

/**
 * Created by Brigham on 11/16/2014.
 */
public class ServicesClass {

    enum CellStatus {
        HIT,
        MISS,
        SHIP,
        NONE
    };
    enum GameStatus {
        DONE,
        WAITING,
        PLAYING
    };
    //region POJO

    public static class NetworkGame {
        public final String id;
        public final String name;
        public final GameStatus status;

        public NetworkGame(String id, String name, GameStatus status) {
            this.id = id;
            this.name = name;
            this.status = status;
        }
    }

    public static class NetworkGameDetail {
        public final String id;
        public final String name;
        public final String player1;
        public final String player2;
        public final String winner;
        public final int missilesLaunched;
        public final GameStatus status;

        public NetworkGameDetail(String id, String name, GameStatus status, String player1, String player2, String winner, int missilesLaunched) {
            this.id = id;
            this.name = name;
            this.status = status;
            this.player1 = player1;
            this.player2 = player2;
            this.winner = winner;
            this.missilesLaunched = missilesLaunched;
        }
    }
    public static class PlayerName {
        public final String playerName;
        public PlayerName(String playerName) {
            this.playerName = playerName;
        }
    }
    public static class JoinGameResponse {
        public final String playerId;

        public JoinGameResponse(String playerId) {
            this.playerId = playerId;
        }
    }

    public static class NewGameResponse {
        public final String playerId;
        public final String gameId;

        public NewGameResponse(String playerId, String gameId) {
            this.playerId = playerId;
            this.gameId = gameId;
        }
    }

    public static class GuessResponse {
        public final boolean hit;
        public final int shipSunk;

        public GuessResponse(boolean hit, int shipSunk) {
            this.hit = hit;
            this.shipSunk = shipSunk;
        }
    }

    public static class CurrentTurnResponse {
        public final boolean isYourTurn;
        public final String winner;
        public CurrentTurnResponse(boolean isYourTurn, String winner) {
            this.isYourTurn = isYourTurn;
            this.winner = winner;
        }
    }

    public static class Guess {
        public final String playerId;
        public final int xPos;
        public final int yPos;

        public Guess(String playerId, int xPos, int yPos) {
            this.playerId = playerId;
            this.xPos = xPos;
            this.yPos = yPos;
        }
    }

    public static class Cell {
        public final int xPos;
        public final int yPos;
        public CellStatus status;

        public Cell(int xPos, int yPos, CellStatus status) {
            this.xPos = xPos;
            this.yPos = yPos;
            this.status = status;
        }
    }

    public static class PlayerBoardResponse {
        public final List<Cell> playerBoard;
        public final List<Cell> opponentBoard;

        public PlayerBoardResponse(List<Cell> playerBoard, List<Cell> opponentBoard) {
            this.playerBoard = playerBoard;
            this.opponentBoard = opponentBoard;
        }
    }

    public static class NewGame {
        public final String gameName;
        public final String playerName;

        public NewGame(String gameName, String playerName) {
            this.gameName = gameName;
            this.playerName = playerName;
        }
    }

    public static class PlayerId {
        public final String playerId;
        public PlayerId(String playerId) {
            this.playerId = playerId;
        }
    }




    //endregion
}
