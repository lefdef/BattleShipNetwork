package edu.cs4962.example.battleshipnetwork;

import android.app.Fragment;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import static edu.cs4962.example.battleshipnetwork.ServicesClass.GameStatus;

/**
 * Created by Brigham on 10/29/2014.
 */
public class GamePlayFragment extends Fragment {

    /*
        TODO: Show name of player underneath the board
     */
    //BattleshipGameModel _game;
    private GridView _my_grid = null;
    private GridView _opponent_grid = null;
    private BoardImageAdapter _my_adapter;
    private BoardImageAdapter _opponent_adapter;
    private TextView _my_label;
    private TextView _opponent_label;
    TableLayout _rootLayout;

    public void refreshPlayersGrids() {
        _rootLayout.setVisibility(View.VISIBLE);
        if(BattleshipGameCollection.getInstance().getCurrentGame().getGameState() == null) {
            // tear everything down
            disableItemClickListeners();
            _rootLayout.setVisibility(View.INVISIBLE);
            return;
        }
        BattleshipGameCollection.getInstance().getCurrentGame().setOnBoardChangeListener(new BattleshipGameModel.OnBoardChangeListener() {
            @Override
            public void OnBoardChanged() {
                refreshPlayersGrids();
            }
        });
        // update the adapter
        _my_label.setText("Player 1 board");
        _opponent_label.setText("Opponent board");

        // re-initialize the board with any new data
        _my_adapter.populateBoard();
        // notify adapter that data has changed and refresh the GridView
        _my_adapter.notifyDataSetChanged();
        _my_grid.invalidateViews();

        // re-initialize the board with any new data
        _opponent_adapter.populateBoard();
        _opponent_adapter.notifyDataSetChanged();
        _opponent_grid.invalidateViews();

        _opponent_grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                if (_onMissileFiredListener != null) {
                    _onMissileFiredListener.OnMissileFired(position);
                }
            }
        });

        if(BattleshipGameCollection.getInstance().getCurrentGame().getGameState() == GameStatus.DONE) {
            Toast.makeText(getActivity(), String.format("%s wins!", BattleshipGameCollection.getInstance().getCurrentGame().getWinner()), Toast.LENGTH_SHORT).show();
            disableItemClickListeners();
            return;
        }
    }

    // called on GameOver
    public void disableItemClickListeners() {
        _my_grid.setOnItemClickListener(null);
        _opponent_grid.setOnItemClickListener(null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        _rootLayout = new TableLayout(getActivity());

        // TEXT
        _my_label = new TextView(getActivity());
        _my_label.setTextSize(30);
        _my_label.setGravity(Gravity.CENTER_HORIZONTAL);

        _opponent_label = new TextView(getActivity());
        _opponent_label.setTextSize(30);
        _opponent_label.setGravity(Gravity.CENTER_HORIZONTAL);

        // BOARD
        _my_grid = new GridView(getActivity());
        _opponent_grid = new GridView(getActivity());
        _my_grid.setNumColumns(10);
        _opponent_grid.setNumColumns(10);
        _my_adapter = new BoardImageAdapter(getActivity(), 0);
        _opponent_adapter = new BoardImageAdapter(getActivity(), 1);
        _my_grid.setAdapter(_my_adapter);
        _opponent_grid.setAdapter(_opponent_adapter);

        _rootLayout.setStretchAllColumns(true);

        // row 1
        TableRow row = new TableRow(getActivity());
        row.addView(_my_label);
        row.addView(_opponent_label);
        _rootLayout.addView(row);

        // row 2
        row = new TableRow(getActivity());
        row.addView(_my_grid);
        row.addView(_opponent_grid);
        _rootLayout.addView(row);

        return _rootLayout;
    }

    //region OnMissileFiredListener

    OnMissileFiredListener _onMissileFiredListener = null;

    public interface OnMissileFiredListener {
        public void OnMissileFired(int position);
    }

    public OnMissileFiredListener getOnMissileFiredListener() {
        return _onMissileFiredListener;
    }

    public void setOnMissleFiredListener(OnMissileFiredListener onMissileFiredListener) {
        this._onMissileFiredListener = onMissileFiredListener;
    }
    //endregion
}
