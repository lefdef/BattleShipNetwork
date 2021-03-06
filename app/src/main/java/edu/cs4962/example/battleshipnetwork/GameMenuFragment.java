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

    final String NEW_LINE = "\r\n";
    // TODO: Delete Game should indicate a number
    LinearLayout _rootLayout;
    ListView _gameListView;
    UUID[] _gameIds = null;
    OnMenuItemSelectedListener _onMenuItemSelectedListener = null;
    OnNewGameSelectedListener _onNewGameSelectedListener = null;
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
                String gameIdentifier = _gameIds[position].toString();
                _gameListPosition = position;

                if (_onMenuItemSelectedListener != null) {
                    _onMenuItemSelectedListener.OnMenuItemSelected(GameMenuFragment.this, gameIdentifier);
                }
            }
        });

        // gameOptionsList
        String[] gameOptionsList = new String[]{
                "New Game"
                //"Delete Current Game"
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
                switch (position) {
                    case 0: // new game
                        if (_onNewGameSelectedListener != null) {
                            _onNewGameSelectedListener.OnNewGameSelected();
                        }
                        break;
                    case 1:
                        // TODO: JOIN GAME??
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
        if (_gameIds == null || _gameIds.length != gameIdentifiers.size()) {
            _gameIds = gameIdentifiers.toArray(new UUID[gameIdentifiers.size()]);
        }
        return BattleshipGameCollection.getInstance().getIdentifiers().size();
    }

    @Override
    public Object getItem(int i) {
        return _gameIds[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        String gameId = _gameIds[(int) getItemId(position)].toString();
        BattleshipGameModel game = BattleshipGameCollection.getInstance().getGame(gameId);

        TextView gameTitleView = new TextView(getActivity());
        gameTitleView.setTextSize(16.0f);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Game: %s", game.getName()));
        sb.append(NEW_LINE);
        sb.append(String.format("Status: %s", game.getGameState()));

        gameTitleView.setHeight(75);
        gameTitleView.setMinimumHeight(75);


        if (gameId.equals(BattleshipGameCollection.getInstance().getCurrentGame().getGameId())) {
            sb.append(NEW_LINE);

            if (game.getGameState() == ServicesClass.GameStatus.DONE) {
                sb.append(String.format("Winner: %s", game.getWinner()));
            } else {
                sb.append(String.format("Turn: %s", game.isMyTurn() ? game.getMyName() : game.getOpponentName()));
            }

            sb.append(NEW_LINE);
            sb.append(String.format("Missiles launched %1$d", game.getMissilesLaunched()));

            gameTitleView.setHeight(160);
            gameTitleView.setMinimumHeight(160);
        }

        gameTitleView.setText(sb.toString());

        if (game.getGameId().equals(BattleshipGameCollection.getInstance().getCurrentGame().getGameId())) {
            gameTitleView.setBackgroundColor(Color.rgb(255, 250, 240));
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

    //region OnMenuItemSelectedListener

    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
    }

    public void refreshGameMenu() {
        _gameListView.invalidateViews();
    }

    public OnMenuItemSelectedListener getOnMenuItemSelectedListener() {
        return _onMenuItemSelectedListener;
    }

    public void setOnMenuItemSelectedListener(OnMenuItemSelectedListener _onMenuItemSelectedListener) {
        this._onMenuItemSelectedListener = _onMenuItemSelectedListener;
    }

    public OnNewGameSelectedListener getOnNewGameSelectedListener() {
        return _onNewGameSelectedListener;
    }

    public void setOnNewGameSelectedListener(OnNewGameSelectedListener _onNewGameSelectedListener) {
        this._onNewGameSelectedListener = _onNewGameSelectedListener;
    }

    public interface OnMenuItemSelectedListener {
        public void OnMenuItemSelected(GameMenuFragment gameMenuFragment, String gameId);
    }

    public interface OnNewGameSelectedListener {
        public void OnNewGameSelected();
    }
    //endregion
}
