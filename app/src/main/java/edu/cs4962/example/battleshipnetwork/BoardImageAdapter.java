package edu.cs4962.example.battleshipnetwork;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * Created by Brigham on 11/1/2014.
 */
public class BoardImageAdapter extends BaseAdapter {
    private Context _context;
    private Integer[] _tileIds;
    private int _boardId = 0;

    public BoardImageAdapter(Context c, int boardId) {
        _context = c;
        _boardId = boardId;
        _tileIds = new Integer[100];
    }

    public int getCount() {
        return _tileIds.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        SquareImageView imageView;
        if (convertView == null) {
            imageView = new SquareImageView(_context);
        } else {
            imageView = (SquareImageView) convertView;
        }

        if(_tileIds[position] != null) { imageView.setImageResource(_tileIds[position]); }
        return imageView;
    }

    //region INITIALIZE_GAME_BOARD
    public void populateBoard() {
        if(BattleshipGameCollection.getInstance().getCurrentGame() == null) { return; }

        int[] board = BattleshipGameCollection.getInstance().getCurrentGame().getBoard(_boardId);


        for (int i = 0; i < board.length; i++) {
            setCell(i, board[i]);
        }
    }

    public void setCell(int position, int state) {
        switch (state) {
            case 1: // miss
                _tileIds[position] = R.drawable.miss;
                break;
            case 3: // missileFired
                _tileIds[position] = R.drawable.explosion;
                break;
            default:
                _tileIds[position] = R.drawable.water_g1_t1;
                break;
        }
    }

    //endregion
}