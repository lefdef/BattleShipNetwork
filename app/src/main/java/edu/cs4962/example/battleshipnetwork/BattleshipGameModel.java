package edu.cs4962.example.battleshipnetwork;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static edu.cs4962.example.battleshipnetwork.ServicesClass.Cell;
import static edu.cs4962.example.battleshipnetwork.ServicesClass.CellStatus;
import static edu.cs4962.example.battleshipnetwork.ServicesClass.CurrentTurnResponse;
import static edu.cs4962.example.battleshipnetwork.ServicesClass.GameStatus;
import static edu.cs4962.example.battleshipnetwork.ServicesClass.NetworkGameDetail;
import static edu.cs4962.example.battleshipnetwork.ServicesClass.PlayerBoardResponse;
import static edu.cs4962.example.battleshipnetwork.ServicesClass.GuessResponse;
import static edu.cs4962.example.battleshipnetwork.ServicesClass.Guess;
import static edu.cs4962.example.battleshipnetwork.ServicesClass.NetworkGame;

/**
 * Created by Brigham on 10/27/2014.
 */

public class BattleshipGameModel {
    private static final String TAG = "BATTLESHIPGAMEMODEL";
    private boolean _isMyTurn;
    private String _gameId;
    private String _playerId;
    private String _gameName;
    private int _missilesLaunched;
    private String _winner;
    private List<Cell> _myBoard;
    private List<Cell> _opponentBoard;
    private String _myName;
    private String _opponentName;
    private GameStatus _state;
    private int[] gameReadiness = new int[5];
    private Guess _guess;

    public BattleshipGameModel(String gameId) {
        setGameId(gameId);
        //LoadDefaultValues();
    }
    public BattleshipGameModel(ServicesClass.NetworkGame game) {
        _gameId = game.id;
        _gameName = game.name;
        _state = game.status;
    }

    public String getWinner() {
        return _winner;
    }

    public GameStatus getGameState() {
        return _state;
    }

    public void setMissileFirePosition(Guess guess) {
        _guess = new Guess(guess.playerId, guess.xPos, guess.yPos);
    }
    public static int coordinateToPosition(int xPos, int yPos) {
        return xPos * 10 + yPos;
    }
    public static int positionToCoordinate(int position) {
        return position / 10 + position % 10;
    }
    public void setMissileFireResponse(GuessResponse guessResponse) {
        if(_guess == null) {
            return;
        }

        int position = coordinateToPosition(_guess.xPos, _guess.yPos);

        if(guessResponse.hit) {
            _opponentBoard.get(position).status = CellStatus.HIT;
        } else {
            _opponentBoard.get(position).status = CellStatus.MISS;
        }

        // clear out the guess
        _guess = null;
    }

    public int getMissilesLaunched() {
        return _missilesLaunched;
    }

    public boolean isMyTurn() {
        return _isMyTurn;
    }

    public String getPlayerTurn() {
        return _isMyTurn ? _myName : _opponentName;
    }

    public String getOpponentName() {
        return _opponentName;
    }
    public String getMyName() {
        return _myName;
    }

    public String getGameId() {
        return _gameId;
    }

    public void setGameId(String identifier) {
        gameReadiness = new int[5];
        gameReadiness[4] = 1;
        _gameId = identifier;
    }

    public void setGameStatus(GameStatus status) {
        _state = status;
    }

    public String getName() {
        return _gameName;
    }
    public List<Cell> getBoard(int boardNum) {
        return boardNum == 0 ? _myBoard : _opponentBoard;
    }

    public String getPlayerId() {
        return _playerId;
    }

    public void setMyPlayerId(String playerId) {
        _playerId = playerId;
        gameReadiness[0] = 1;
    }

    int _lastTurn = -1;

    public void setCurrentTurn(CurrentTurnResponse currentTurn) {
        int currentTurnInt = currentTurn.isYourTurn ? 1 : 0;
        boolean turnChanged = _lastTurn != currentTurnInt;
        _lastTurn = currentTurnInt;

        _isMyTurn = currentTurn.isYourTurn;
        if(currentTurn.winner.equals("IN PROGRESS")) {
            _state = GameStatus.PLAYING;
        } else {
            _winner = currentTurn.winner;
            _state = GameStatus.DONE;
        }

        gameReadiness[1] = 1;
        if(turnChanged && _onTurnChangedListener != null) {
            _onTurnChangedListener.OnTurnChanged();
        }
    }

    public void setBoards(PlayerBoardResponse boards) {
        _myBoard = new ArrayList<Cell>(boards.playerBoard);
        _opponentBoard = new ArrayList<Cell>(boards.opponentBoard);
        gameReadiness[2] = 1;
    }

    public void setGameDetail(NetworkGameDetail gameDetail) {
        if (!gameDetail.id.equals(_gameId)) {
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
        isGameReady();
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

    //region Listeners
    public OnBoardChangeListener _onBoardChangedListener;
    public OnBoardChangeListener getOnBoardChangeListener() { return _onBoardChangedListener; }
    public void setOnBoardChangeListener(OnBoardChangeListener _onBoardChangeListener) { this._onBoardChangedListener = _onBoardChangeListener; }
    public interface OnBoardChangeListener { public void OnBoardChanged(); }

    public OnTurnChangedListener _onTurnChangedListener;
    public OnTurnChangedListener getOnTurnChangedListener() { return _onTurnChangedListener; }
    public void setOnTurnChangedListener(OnTurnChangedListener _onTurnChangedListener) { this._onTurnChangedListener = _onTurnChangedListener; }
    public interface OnTurnChangedListener { public void OnTurnChanged(); }
    //endregion
}