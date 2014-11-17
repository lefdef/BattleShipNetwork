package edu.cs4962.example.battleshipnetwork;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.List;
import java.util.UUID;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

import static edu.cs4962.example.battleshipnetwork.ServicesClass.CurrentTurnResponse;
import static edu.cs4962.example.battleshipnetwork.ServicesClass.Guess;
import static edu.cs4962.example.battleshipnetwork.ServicesClass.GuessResponse;
import static edu.cs4962.example.battleshipnetwork.ServicesClass.JoinGameResponse;
import static edu.cs4962.example.battleshipnetwork.ServicesClass.NetworkGame;
import static edu.cs4962.example.battleshipnetwork.ServicesClass.NetworkGameDetail;
import static edu.cs4962.example.battleshipnetwork.ServicesClass.NewGame;
import static edu.cs4962.example.battleshipnetwork.ServicesClass.NewGameResponse;
import static edu.cs4962.example.battleshipnetwork.ServicesClass.PlayerBoardResponse;

/**
 * Created by Brigham on 10/29/2014.
 */
public class GameActivity extends Activity {

    private static final String API_URL = "http://battleship.pixio.com";
    private final String TAG = "GAMEACTIVITY";
    private RestAdapter restAdapter;
    private BattleshipService battleshipService;
    private GamePlayFragment _gamePlayFragment;
    private GameMenuFragment _gameMenuFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.HORIZONTAL);
        setContentView(rootLayout);

        // create layout for game menu fragment
        FrameLayout gameMenuLayout = new FrameLayout(this);
        gameMenuLayout.setId(10);
        gameMenuLayout.setBackgroundColor(Color.WHITE);
        rootLayout.addView(gameMenuLayout,
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        20
                ));

        // create layout for game play fragment
        FrameLayout gamePlayLayout = new FrameLayout(this);
        gamePlayLayout.setId(11);
        gamePlayLayout.setBackgroundColor(Color.rgb(173, 216, 230));

        // add fragment layouts to root layout
        rootLayout.addView(gamePlayLayout,
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        80
                ));
        // create fragments
        _gameMenuFragment = new GameMenuFragment();
        _gamePlayFragment = new GamePlayFragment();

        // add fragments to fragment manager
        FragmentTransaction addTransaction = getFragmentManager().beginTransaction();
        addTransaction.add(10, _gameMenuFragment);
        addTransaction.add(11, _gamePlayFragment);
        addTransaction.commit();

        //region CONTROLLER
        _gameMenuFragment.setOnNewGameSelectedListener(new GameMenuFragment.OnNewGameSelectedListener() {
            @Override
            public void OnNewGameSelected() {
                NewGame newGame = new NewGame("TheCakeIsALie", "Dave");
                battleshipService.createNewGame(newGame, new Callback<NewGameResponse>() {
                    @Override
                    public void success(NewGameResponse newGameResponse, Response response) {
                        // TODO: HANDLE GAME WAITING
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {

                    }
                });
            }
        });

        _gameMenuFragment.setOnMenuItemSelectedListener(new GameMenuFragment.OnMenuItemSelectedListener() {
            @Override
            public void OnMenuItemSelected(GameMenuFragment gameMenuFragment, UUID identifier) {
                // TODO: make sure you can even join the current game

                // first check to make sure menu item selected is not already the current game
                if (BattleshipGameCollection.getInstance().IsCurrentGame(identifier)) {
                    return;
                }

                BattleshipGameCollection.getInstance().pendingCurrentGameSet(identifier);

                battleshipService.joinGame(identifier.toString(), "testplayer", new Callback<JoinGameResponse>() {
                    @Override
                    public void success(JoinGameResponse joinGameResponse, Response response) {
                        BattleshipGameCollection.getInstance().getCurrentGame().setPlayerId(joinGameResponse.playerId);

                        battleshipService.determineTurn(joinGameResponse.playerId.toString(), new Callback<CurrentTurnResponse>() {
                            @Override
                            public void success(CurrentTurnResponse currentTurnResponse, Response response) {
                                BattleshipGameCollection.getInstance().getCurrentGame().setCurrentTurn(currentTurnResponse);
                            }

                            @Override
                            public void failure(RetrofitError retrofitError) {

                            }
                        });

                        battleshipService.requestBoard(joinGameResponse.playerId.toString(), new Callback<PlayerBoardResponse>() {
                            @Override
                            public void success(PlayerBoardResponse playerBoardResponse, Response response) {
                                BattleshipGameCollection.getInstance().getCurrentGame().setBoards(playerBoardResponse);
                            }

                            @Override
                            public void failure(RetrofitError retrofitError) {

                            }
                        });
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {

                    }
                });

                battleshipService.gameDetail(BattleshipGameCollection.getInstance().getCurrentGame().getIdentifier().toString(), new Callback<NetworkGameDetail>() {
                    @Override
                    public void success(NetworkGameDetail networkGameDetail, Response response) {
                        BattleshipGameCollection.getInstance().getCurrentGame().setGameDetail(networkGameDetail);

                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {

                    }
                });

                //Toast.makeText(getApplicationContext(), "Current game changed!", Toast.LENGTH_SHORT).show();
            }
        });

        _gamePlayFragment.setOnMissleFiredListener(new GamePlayFragment.OnMissileFiredListener() {
            @Override
            public void OnMissileFired(int position) {
                battleshipService.guess(BattleshipGameCollection.getInstance().getCurrentGame().getPlayerId.toString(),

                        );
                Player turn = BattleshipGameCollection.getInstance().getCurrentGame().getPlayerTurn();
                boolean success = BattleshipGameCollection.getInstance().getCurrentGame().missileFired(position);
                //TODO: Move string to string.xml
                _gameMenuFragment.refreshGameMenu();
                Toast.makeText(getApplicationContext(),
                        String.format("     %1$s\r\n\r\nPass device to player %2$s", (success ? "Target hit!" : "Target missed!"), (turn == Player.Player1 ? "2" : "1")),
                        Toast.LENGTH_SHORT).show();
            }
        });

        BattleshipGameCollection.getInstance().setOnGameSetChangedListener(new BattleshipGameCollection.OnGameSetChangedListener() {
            @Override
            public void onGameSetChanged() {
                //_gamePlayFragment.refreshPlayersGrids();
                _gameMenuFragment.refreshGameMenu();
            }
        });
    }

    private void refreshGamesList() {
        battleshipService.gamesList(new Callback<List<NetworkGame>>() {
            @Override
            public void success(List<NetworkGame> networkGames, Response response) {

            }

            @Override
            public void failure(RetrofitError retrofitError) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        BattleshipGameCollection.getInstance().clearGames();
        Log.i(TAG, "onResume()");

        restAdapter = new RestAdapter.Builder()
                .setEndpoint(API_URL)
                .build();

        if (battleshipService == null) {
            battleshipService = restAdapter.create(BattleshipService.class);
        }

        battleshipService.gamesList(new Callback<List<NetworkGame>>() {
            @Override
            public void success(List<NetworkGame> networkGames, Response response) {
                BattleshipGameCollection.getInstance().addNetworkGames(networkGames);
            }

            @Override
            public void failure(RetrofitError retrofitError) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause()");

        try {
            BattleshipGameCollection.getInstance().clearGames();
        } catch (Exception e) {
            Log.e("exception", e.getMessage());
        }
    }

    interface BattleshipService {
        @GET("/api/games/")
        void gamesList(Callback<List<NetworkGame>> gamesListCallback);

        @GET("/api/games/{id}")
        void gameDetail(@Path("id") String id, Callback<NetworkGameDetail> gameDetailCallback);

        @POST("/api/games/{id}/join")
        void joinGame(@Path("id") String id, @Body String playerName, Callback<JoinGameResponse> gameResponseCallback);

        @POST("/api/games")
        void createNewGame(@Body NewGame newGame, Callback<NewGameResponse> newGameResponseCallback);

        @POST("/api/games/{id}/guess")
        void guess(@Path("id") String playerId, @Body Guess guess, Callback<GuessResponse> guessResponseCallback);

        @POST("/api/games/{id}/status")
        void determineTurn(@Path("id") String playerId, Callback<CurrentTurnResponse> currentTurnResponseCallback);

        @POST("/api/games/{id}/board")
        void requestBoard(@Path("id") String playerId, Callback<PlayerBoardResponse> playerBoardResponseCallback);
    }
}