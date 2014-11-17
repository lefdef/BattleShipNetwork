package edu.cs4962.example.battleshipnetwork;

import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static edu.cs4962.example.battleshipnetwork.ServicesClass.Cell;
import static edu.cs4962.example.battleshipnetwork.ServicesClass.CurrentTurnResponse;
import static edu.cs4962.example.battleshipnetwork.ServicesClass.GameStatus;
import static edu.cs4962.example.battleshipnetwork.ServicesClass.NetworkGameDetail;
import static edu.cs4962.example.battleshipnetwork.ServicesClass.PlayerBoardResponse;

/**
 * Created by Brigham on 10/27/2014.
 */

public class BattleshipGameModel {
    private static final String TAG = "BATTLESHIPGAMEMODEL";
    /*  TODO: Getter for number of boats pieces missileFired and total number of boat pieces left. For both players. Probably an array with stats
        TODO: Getter for status (string). Should indicate the following "Player X turn or Game Over
     */
    public OnBoardChangeListener _onBoardChangedListener;
    private ArrayList<int[]> _boards;
    private boolean _isMyTurn;
    private String _gameId;
    private String _playerId;
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
    private GameStatus _state;
    private int[] gameReadiness = new int[5];

    public BattleshipGameModel(String gameId) {
        setGameId(gameId);
        //LoadDefaultValues();
    }

    //region Getters & Setters
    public String getWinner() {
        return _winner;
    }

    public GameStatus getGameState() {
        return _state;
    }

    public int getMissilesLaunched() {
        return _missilesLaunched;
    }

    public String getPlayerTurn() {
        return _isMyTurn ? _myName : _opponentName;
    }

    public String getIdentifier() {
        return _gameId;
    }

    public void setGameId(String identifier) {
        gameReadiness = new int[5];
        gameReadiness[4] = 1;
        _gameId = identifier;
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

    public int player2_missilesFired() {
        return _missilesFired[1];
    }
    //endregion

    public int[] getBoard() {
        return _boards.get(_currentTurn);
    }

    public int[] getBoard(int boardNum) {
        return _boards.get(boardNum);
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

    public String getPlayerId() {
        return _playerId;
    }

    public void setPlayerId(String playerId) {
        _playerId = playerId;
        gameReadiness[0] = 1;
    }

    public void setCurrentTurn(CurrentTurnResponse currentTurn) {
        _isMyTurn = currentTurn.isYourTurn;
        if(currentTurn.winner == "IN PROGRESS") {
            _state = GameStatus.PLAYING;
        } else {
            _winner = currentTurn.winner;
            _state = GameStatus.DONE;
        }

        gameReadiness[1] = 1;
    }

    public void setBoards(PlayerBoardResponse boards) {
        _myBoard = new ArrayList<Cell>(boards.playerBoard);
        _opponentBoard = new ArrayList<Cell>(boards.opponentBoard);
        gameReadiness[2] = 1;
    }

    public void setGameDetail(NetworkGameDetail gameDetail) {
        if (gameDetail.id.equals(_gameId)) {
            Log.e(TAG, "Game ids do not match! currentGameId=" + _gameId.toString() + ", gameDetailId=" + gameDetail.id.toString());
            return;
        }

        _gameName = gameDetail.name;
        _myName = gameDetail.player1;
        _opponentName = gameDetail.player2;
        _missilesLaunched = gameDetail.missilesLaunched;

        if (gameDetail.winner.equals("IN PROGRESS")) {
            _state = GameStatus.PLAYING;
        } else {
            _winner = gameDetail.winner;
            _state = GameStatus.DONE;
        }

        gameReadiness[3] = 1;
    }

    private boolean isGameReady() {
        boolean ready = true;
        for (int i = 0; i < gameReadiness.length; i++) {
            if (gameReadiness[i] != 1) {
                ready = false;
                break;
            }
        }
        if (ready && _onBoardChangedListener != null) {
            _onBoardChangedListener.OnBoardChanged();
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

    //region OnBoardChangedListener

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

    public OnBoardChangeListener getOnBoardChangeListener() {
        return _onBoardChangedListener;
    }

    public void setOnBoardChangeListener(OnBoardChangeListener _onBoardChangeListener) {
        this._onBoardChangedListener = _onBoardChangeListener;
    }

    private void triggerObservers() {
        if (_onBoardChangedListener != null) {
            _onBoardChangedListener.OnBoardChanged();
        }
    }
    //endregion

    public interface OnBoardChangeListener {
        public void OnBoardChanged();
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