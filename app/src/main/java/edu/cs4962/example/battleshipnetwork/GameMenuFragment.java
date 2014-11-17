package edu.cs4962.example.battleshipnetwork;

import android.app.Fragment;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Set;
import java.util.UUID;

public class GameMenuFragment extends Fragment implements ListAdapter {

    // TODO: Delete Game should indicate a number
    LinearLayout _rootLayout;
    ListView _gameListView;
    UUID[] _games = null;
    private int _gameListPosition = 0;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // rootlayout
        _rootLayout = new LinearLayout(getActivity());
        _rootLayout.setOrientation(LinearLayout.VERTICAL);

        // _gameListView
        _gameListView = new ListView(getActivity());
        _gameListView.setAdapter(this);
        _gameListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                UUID gameIdentifier = _games[position];
                _gameListPosition = position;

                if (_onMenuItemSelectedListener != null) {
                    _onMenuItemSelectedListener.OnMenuItemSelected(GameMenuFragment.this, gameIdentifier);
                }
            }
        });

        // gameOptionsList
        String[] gameOptionsList = new String[]{
                "New Game",
                "Delete Current Game"
        };

        ArrayAdapter<String> gameMenuOptionsAdapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.simple_list_item_1,
                gameOptionsList
        );
        // gameOptionsListView
        ListView gameOptionsListView = new ListView(getActivity());
        gameOptionsListView.setBackgroundColor(Color.LTGRAY);
        gameOptionsListView.setAdapter(gameMenuOptionsAdapter);

        gameOptionsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                switch(position) {
                    case 0: // new game
                        BattleshipGameModel newGame = new BattleshipGameModel();
                        BattleshipGameCollection.getInstance().addGame(newGame);
                        break;
                    case 1: // TODO: properly delete game
                        if(_games.length > 0) {
                            BattleshipGameCollection.getInstance().removeGame(_games[_gameListPosition]);
                        }
                        break;
                }
            }
        });

        // _rootLayout
        _rootLayout.addView(gameOptionsListView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0
        ));

        _rootLayout.addView(_gameListView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1
        ));
        return _rootLayout;
    }

    @Override
    public int getCount() {
        // TODO: consider ordering the list by games in progress or game over
        Set<UUID> gameIdentifiers = BattleshipGameCollection.getInstance().getIdentifiers();
        if (_games == null || _games.length != gameIdentifiers.size()) {
            _games = gameIdentifiers.toArray(new UUID[gameIdentifiers.size()]);
        }
        return BattleshipGameCollection.getInstance().getIdentifiers().size();
    }

    @Override
    public Object getItem(int i) {
        return _games[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    final String NEW_LINE = "\r\n";
    final String DIVIDER = "GAME #";
    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        UUID gameIdentifier = _games[(int) getItemId(position)];
        BattleshipGameModel game = BattleshipGameCollection.getInstance().getGame(gameIdentifier);

        TextView gameTitleView = new TextView(getActivity());
        gameTitleView.setTextSize(16.0f);
        // TODO: create accessors to get more info from BattleshipGameModel
        StringBuilder sb = new StringBuilder();
        sb.append(DIVIDER + (++position));
        sb.append(NEW_LINE);

        if(game.getGameState() == GamePlayState.GAME_OVER) {
            sb.append("Status: GAME OVER");
        } else {
            sb.append(String.format("Status: %s turn", game.getPlayerTurn()));
        }

        sb.append(NEW_LINE);
        sb.append(String.format("P1 missiles fired: %1$d, hit: %2$d", game.player1_missilesFired(), game.player1_totalHits()));
        sb.append(NEW_LINE);
        sb.append(String.format("P2 missiles fired: %1$d, hit: %2$d", game.player2_missilesFired(), game.player2_totalHits()));

        gameTitleView.setText(sb.toString());
        gameTitleView.setHeight(200);
        gameTitleView.setMinimumHeight(200);
        if(game == BattleshipGameCollection.getInstance().getCurrentGame()) {
            gameTitleView.setBackgroundColor(Color.rgb(255,250,240));
        }
        return gameTitleView;
    }

    @Override
    public int getItemViewType(int i) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return getCount() <= 0;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int i) {
        return true;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver dataSetObserver) {
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
    }

    public void refreshGameMenu() {
        _gameListView.invalidateViews();
    }

    //region OnMenuItemSelectedListener

    OnMenuItemSelectedListener _onMenuItemSelectedListener = null;

    public interface OnMenuItemSelectedListener {
        public void OnMenuItemSelected(GameMenuFragment gameMenuFragment, UUID identifier);
    }

    public OnMenuItemSelectedListener getOnMenuItemSelectedListener() {
        return _onMenuItemSelectedListener;
    }

    public void setOnMenuItemSelectedListener(OnMenuItemSelectedListener _onMenuItemSelectedListener) {
        this._onMenuItemSelectedListener = _onMenuItemSelectedListener;
    }
    //endregion
}
