package edu.cs4962.example.battleshipnetwork;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.List;
import static edu.cs4962.example.battleshipnetwork.ServicesClass.Cell;
import static edu.cs4962.example.battleshipnetwork.ServicesClass.CellStatus;

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
        List<Cell> board = BattleshipGameCollection.getInstance().getCurrentGame().getBoard(_boardId);

        for(Cell c : board) {
            setCell(c);
        }
    }

    public void setCell(Cell cell) {
        int position = cell.xPos * 10 + cell.yPos;

        switch (cell.status) {
            case MISS: // miss
                _tileIds[position] = R.drawable.miss;
                break;
            case HIT: // missileFired
                _tileIds[position] = R.drawable.explosion;
                break;
            case SHIP:
                _tileIds[position] = R.drawable.ship;
                break;
            default:
                _tileIds[position] = R.drawable.water_g1_t1;
                break;
        }
    }

    //endregion
}