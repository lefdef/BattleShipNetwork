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
    private GridView _player1_grid = null;
    private GridView _player2_grid = null;
    private BoardImageAdapter _player1_adapter;
    private BoardImageAdapter _player2_adapter;
    private TextView _player1_label;
    private TextView _player2_label;
    TableLayout _rootLayout;

    public void refreshPlayersGrids() {
        _rootLayout.setVisibility(View.VISIBLE);
        if(BattleshipGameCollection.getInstance().getCurrentGame() == null) {
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
        _player1_label.setText("Player 1 board");
        _player2_label.setText("Opponent board");

        // re-initialize the board with any new data
        _player1_adapter.populateBoard();
        // notify adapter that data has changed and refresh the GridView
        _player1_adapter.notifyDataSetChanged();
        _player1_grid.invalidateViews();

        // re-initialize the board with any new data
        _player2_adapter.populateBoard();
        _player2_adapter.notifyDataSetChanged();
        _player2_grid.invalidateViews();

        if (BattleshipGameCollection.getInstance().getCurrentGame().isMyTurn()) {
            _player1_grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Toast.makeText(getActivity(), "You're Doing It Wrong. Launch missiles at opponent board!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if(BattleshipGameCollection.getInstance().getCurrentGame().getGameState() == GameStatus.DONE) {
            Toast.makeText(getActivity(), String.format("%s wins!", BattleshipGameCollection.getInstance().getCurrentGame().getWinner()), Toast.LENGTH_SHORT).show();
            disableItemClickListeners();
            return;
        }
    }

    // called on GameOver
    public void disableItemClickListeners() {
        _player1_grid.setOnItemClickListener(null);
        _player2_grid.setOnItemClickListener(null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        _rootLayout = new TableLayout(getActivity());

        // TEXT
        _player1_label = new TextView(getActivity());
        _player1_label.setTextSize(30);
        _player1_label.setGravity(Gravity.CENTER_HORIZONTAL);

        _player2_label = new TextView(getActivity());
        _player2_label.setTextSize(30);
        _player2_label.setGravity(Gravity.CENTER_HORIZONTAL);

        // BOARD
        _player1_grid = new GridView(getActivity());
        _player2_grid = new GridView(getActivity());
        _player1_grid.setNumColumns(10);
        _player2_grid.setNumColumns(10);
        _player1_adapter = new BoardImageAdapter(getActivity(), 0);
        _player2_adapter = new BoardImageAdapter(getActivity(), 1);
        _player1_grid.setAdapter(_player1_adapter);
        _player2_grid.setAdapter(_player2_adapter);

        _rootLayout.setStretchAllColumns(true);

        // row 1
        TableRow row = new TableRow(getActivity());
        row.addView(_player1_label);
        row.addView(_player2_label);
        _rootLayout.addView(row);

        // row 2
        row = new TableRow(getActivity());
        row.addView(_player1_grid);
        row.addView(_player2_grid);
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
