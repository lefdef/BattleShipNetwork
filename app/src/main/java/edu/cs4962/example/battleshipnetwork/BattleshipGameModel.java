package edu.cs4962.example.battleshipnetwork;

import android.util.Log;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static edu.cs4962.example.battleshipnetwork.ServicesClass.NetworkGameDetail;
import static edu.cs4962.example.battleshipnetwork.ServicesClass.PlayerBoardResponse;
import static edu.cs4962.example.battleshipnetwork.ServicesClass.CurrentTurnResponse;
import static edu.cs4962.example.battleshipnetwork.ServicesClass.Cell;

/**
 * Created by Brigham on 10/27/2014.
 */
enum GamePlayState {
    @SerializedName("0")
    IN_PROGRESS,
    @SerializedName("1")
    GAME_OVER
}

enum CellState {
    @SerializedName("0")
    NO_SHIP,
    @SerializedName("1")
    SHIP_MISSED,
    @SerializedName("2")
    SHIP,
    @SerializedName("3")
    SHIP_HIT
}

enum Player {
    @SerializedName("0")
    Player1,
    @SerializedName("1")
    Player2,
    @SerializedName("2")
    HAL
}

public class BattleshipGameModel {
    private static final String TAG = "BATTLESHIPGAMEMODEL";
    /*  TODO: Getter for number of boats pieces missileFired and total number of boat pieces left. For both players. Probably an array with stats
        TODO: Getter for status (string). Should indicate the following "Player X turn or Game Over
     */

    private ArrayList<int[]> _boards;
    private boolean _isMyTurn;
    private UUID _identifier;
    private String _gameName;
    private int[] _totalHits;
    private int[] _missilesFired;
    private int _missilesLaunched;
    private LinkedList<Integer> _shipList;
    private int _totalShipTargets;
    private String _winner;
    private List<Cell> _myBoard;
    private List<Cell> _opponentBoard;
    private String _myName;
    private String _opponentName;

    private GamePlayState _state;
    private Player _players[];

    //region Getters & Setters
    public GamePlayState getGameState() {
        return _state;
    }
    public Player getPlayerTurn() {
        return _players[_currentTurn];
    }
    public UUID getIdentifier() {
        return _identifier;
    }
    public void setIdentifier(UUID identifier) {
        _identifier = identifier;
    }
    public String getName() {
        return _gameName;
    }
    public int player1_totalHits() {
        return _totalHits[0];
    }
    public int player1_missilesFired() {
        return _missilesFired[0];
    }
    public int player2_totalHits() {
        return _totalHits[1];
    }
    public int player2_missilesFired() { return _missilesFired[1]; }
    public int[] getBoard() {
        return _boards.get(_currentTurn);
    }
    public int[] getBoard(int boardNum) {
        return _boards.get(boardNum);
    }
    private GamePlayState UpdateGameStatus() {
        return GamePlayState.IN_PROGRESS;
    }
    private int[] gameReadiness = new int[5];
    //endregion

    public BattleshipGameModel(UUID identifier) {
        this._identifier = identifier;
        gameReadiness[4] = 1;
        //LoadDefaultValues();
    }

    private void LoadDefaultValues() {
//        _shipList = new LinkedList<Integer>(Arrays.asList(2, 3, 3, 4, 5));
//        for(Integer shipLength : _shipList) {
//            _totalShipTargets += shipLength;
//        }
//
//        _gameName = _state == GamePlayState.GAME_OVER ? "Game Over!" : "In Progress!";
//        _boards = new ArrayList<int[]>();
//        _boards.add(new int[100]);
//        _boards.add(new int[100]);
//        _state = GamePlayState.IN_PROGRESS;
//        _totalHits = new int[2];
//        _missilesFired = new int[2];
//        _currentTurn = 0;
//        _players = new Player[]{Player.Player1, Player.Player2};
        //CreateBoards();
    }

    // randomly generates a board with ship pieces
    private void buildGame() {
    }

    public void setPlayerId(UUID playerId) {
        gameReadiness[0] = 1;
    }
    public void setCurrentTurn(CurrentTurnResponse currentTurn) {
        _isMyTurn = currentTurn.isYourTurn;
        if(currentTurn.winner == "IN PROGRESS") {
            _state = GamePlayState.IN_PROGRESS;
        } else {
            _winner = currentTurn.winner;
            _state = GamePlayState.GAME_OVER;
        }

        gameReadiness[1] = 1;
    }
    public void setBoards(PlayerBoardResponse boards) {
        _myBoard = new ArrayList<Cell>(boards.playerBoard);
        _opponentBoard = new ArrayList<Cell>(boards.opponentBoard);
        gameReadiness[2] = 1;
    }

    public void setGameDetail(NetworkGameDetail gameDetail) {
        if(gameDetail.id != _identifier) {
            Log.e(TAG, "Game ids do not match! currentGameId=" + _identifier.toString() + ", gameDetailId=" + gameDetail.id.toString());
            return;
        }

        _gameName = gameDetail.name;
        _myName = gameDetail.player1;
        _opponentName = gameDetail.player2;
        _missilesLaunched = gameDetail.missilesLaunched;

        if(gameDetail.winner == "IN PROGRESS") {
            _state = GamePlayState.IN_PROGRESS;
        } else {
            _winner = gameDetail.winner;
            _state = GamePlayState.GAME_OVER;
        }

        gameReadiness[3] = 1;
    }

    private boolean isGameReady() {
        boolean ready = true;
        for(int i = 0; i < gameReadiness.length; i++) {
            if(gameReadiness[i] != 1) {
                ready = false;
                break;
            }
        }
        return ready;
    }

    private boolean IsOccupied(int boardNum, int position) {
        return _boards.get(boardNum)[position] == 2; // no ship
    }

    private void setShip(int boardNum, int orientation, int boatSize, int position) {
//        for (int i = 0; i < boatSize; i++) {
//            // since columns are 10 blocks then next row would be i * 10
//            int offset = orientation == 0 ? i : i * 10;
//            _boards.get(boardNum)[position + offset] = 2; // ship
//        }
    }

    private void setSuccessfulHit(int position) {
        _boards.get(_currentTurn)[position] = 3; // ship missileFired
        _totalHits[_currentTurn == 0 ? 1 : 0]++;
        checkGameState();
    }

    private void checkGameState() {
        if (_totalHits[_currentTurn == 0 ? 1 : 0] >= _totalShipTargets) {
            _currentTurn = _currentTurn == 0 ? 1 : 0; // change player turn since loser is currently selected
            _state = GamePlayState.GAME_OVER;
        }
    }

    private void setUnsuccessfulHit(int position) {
        _boards.get(_currentTurn)[position] = 1; // ship missed
    }

    public boolean missileFired(int position) {
        Log.i("PLAYER TURN / MISSILE LOC", "firedMissileAt=" + _currentTurn + ",position=" + position);
        // make sure player isn't firing in the same location twice
        //if(!IsValid(_currentTurn, position)) { return false; }
        _missilesFired[_currentTurn]++;
        _currentTurn = _currentTurn == 0 ? 1 : 0;
        boolean success = false;


        if (IsOccupied(_currentTurn, position)) {
            setSuccessfulHit(position);
            success = true;
        } else {
            setUnsuccessfulHit(position);
        }

        triggerObservers();
        return success;
    }

    //region OnBoardChangedListener

    public interface OnBoardChangeListener {
        public void OnBoardChanged();
    }

    public OnBoardChangeListener getOnBoardChangeListener() {
        return _onBoardChangedListener;
    }

    public void setOnBoardChangeListener(OnBoardChangeListener _onBoardChangeListener) {
        this._onBoardChangedListener = _onBoardChangeListener;
    }

    public OnBoardChangeListener _onBoardChangedListener;
    //endregion

    private void triggerObservers() {
        if(_onBoardChangedListener != null) {
            _onBoardChangedListener.OnBoardChanged();
        }
    }

    public static class Game {
        public final UUID id;
        public final String name;
        public final String player1;
        public final String player2;
        public final String winner;
        public final int missilesLaunched;

        public Game(UUID id, String name, String player1, String player2, String winner, int missilesLaunched) {
            this.id = id;
            this.name = name;
            this.player1 = player1;
            this.player2 = player2;
            this.winner = winner;
            this.missilesLaunched = missilesLaunched;
        }
    }
}