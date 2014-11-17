package edu.cs4962.example.battleshipnetwork;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Brigham on 11/16/2014.
 */
public class BattleshipGameCollection {

    private Map<UUID, BattleshipGameModel> _games = new HashMap<UUID, BattleshipGameModel>();
    private BattleshipGameModel _currentGame;

    // keep constructor private from other classes
    private BattleshipGameCollection() {}

    public static BattleshipGameCollection getInstance() { return BattleshipGameCollectionHolder.INSTANCE; }
    public BattleshipGameModel getCurrentGame() { return _currentGame; }
    public boolean IsCurrentGame(UUID game_identifier) { return _games.get(game_identifier) == _currentGame; }
    public Map<UUID, BattleshipGameModel> getGameMap() { return _games; }
    public List<BattleshipGameModel> getGameList() {
        List<BattleshipGameModel> gameList = new ArrayList<BattleshipGameModel>();
        for(UUID uuid : _games.keySet()) {
            gameList.add(_games.get(uuid));
        }
        return gameList;
    }
    public Set<UUID> getIdentifiers() {
        return _games.keySet();
    }
    public BattleshipGameModel getGame(UUID identifier) {
        return _games.get(identifier);
    }

    public void pendingCurrentGameSet(UUID game_identifier) {
        _currentGame = _games.get(game_identifier);
        _currentGame.buildGame();
        if(_onGameSetChangedListener != null) { _onGameSetChangedListener.onGameSetChanged(); }
    }

    public void addBattleshipGameModels(List<ServicesClass.NetworkGame> games) {
        for(int idx = games.size() - 1; idx >= 0; idx--){
            // reverse order
            //_games.put(games.get(idx).id, games.get(idx));
        }

        if(_onGameSetChangedListener != null) { _onGameSetChangedListener.onGameSetChanged(); }
    }

    public void clearGames() {
        if(_games != null) { _games.clear(); }
    }

    /**
     * BattleshipGameCollectionHolder is loaded on the first execution of BattleshipGameCollection.getInstance()
     * or the first access to BattleshipGameCollectionHolder.INSTANCE, not before.
     */
    private static class BattleshipGameCollectionHolder {
        private static final BattleshipGameCollection INSTANCE = new BattleshipGameCollection();
    }

    //region OnGameSetChangedListener

    public interface OnGameSetChangedListener {
        public void onGameSetChanged();
    }

    public OnGameSetChangedListener getOnGameSetChangedListener() {
        return _onGameSetChangedListener;
    }

    public void setOnGameSetChangedListener(OnGameSetChangedListener _onGameSetChangedListener) {
        this._onGameSetChangedListener = _onGameSetChangedListener;
    }

    public OnGameSetChangedListener _onGameSetChangedListener = null;
    // endregion
}