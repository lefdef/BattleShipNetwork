package edu.cs4962.example.battleshipnetwork;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
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
import static edu.cs4962.example.battleshipnetwork.ServicesClass.PlayerName;
import static edu.cs4962.example.battleshipnetwork.ServicesClass.PlayerId;

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
                NewGame newGame = new NewGame("TheCakeIsALie", "Dave1");
                battleshipService.createNewGame(newGame, new Callback<NewGameResponse>() {
                    @Override
                    public void success(NewGameResponse newGameResponse, Response response) {
                        // TODO: HANDLE GAME WAITING
                        BattleshipGameModel newGame = new BattleshipGameModel(newGameResponse.gameId);
                        newGame.setMyPlayerId(newGameResponse.playerId);
                        BattleshipGameCollection.getInstance().setCurrentGame(newGame);
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        Log.i(TAG, "createNewGame, retrofitError=" + retrofitError.getResponse().toString());
                    }
                });
            }
        });

        _gameMenuFragment.setOnMenuItemSelectedListener(new GameMenuFragment.OnMenuItemSelectedListener() {
            @Override
            public void OnMenuItemSelected(GameMenuFragment gameMenuFragment, String gameId) {
                // TODO: make sure you can even join the current game

                // first check to make sure menu item selected is not already the current game
                if (BattleshipGameCollection.getInstance().getCurrentGame().getGameId().equals(gameId)) {
                    return;
                }

                BattleshipGameCollection.getInstance().joinGame(UUID.fromString(gameId));
                PlayerName playerName = new PlayerName("Dave2");

                battleshipService.joinGame(gameId.toString(), "Dave2", new Callback<JoinGameResponse>() {
                    @Override
                    public void success(JoinGameResponse joinGameResponse, Response response) {
                        BattleshipGameCollection.getInstance().getCurrentGame().setMyPlayerId(joinGameResponse.playerId);
                        Log.i(TAG, "game joined!");

                        if(pollCurrentTurn != null) {
                            pollCurrentTurn.cancel(true);
                        }

                        pollCurrentTurn();
                        refreshBoard();
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        Log.i(TAG, "joinGame, retrofitError=" + retrofitError.getResponse().toString());
                    }
                });

                battleshipService.gameDetail(BattleshipGameCollection.getInstance().getCurrentGame().getGameId(), new Callback<NetworkGameDetail>() {
                    @Override
                    public void success(NetworkGameDetail networkGameDetail, Response response) {
                        BattleshipGameCollection.getInstance().getCurrentGame().setGameDetail(networkGameDetail);
                        Log.i(TAG, "game detail received!");

                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        Log.e(TAG, "gameDetail, retrofitError=" + retrofitError.getResponse());
                    }
                });

                //Toast.makeText(getApplicationContext(), "Current game changed!", Toast.LENGTH_SHORT).show();
            }
        });

        _gamePlayFragment.setOnMissleFiredListener(new GamePlayFragment.OnMissileFiredListener() {
            @Override
            public void OnMissileFired(int position) {
                Guess guess = new Guess(BattleshipGameCollection.getInstance().getCurrentGame().getPlayerId(), position/10, position % 10);
                        battleshipService.guess(guess.playerId, guess, new Callback<GuessResponse>() {
                            @Override
                            public void success(GuessResponse guessResponse, Response response) {
                                BattleshipGameCollection.getInstance().getCurrentGame().setMissileFireResponse(guessResponse);
                                Toast.makeText(getApplicationContext(),
                                        String.format(guessResponse.hit ? "Target hit!" : "Target missed!"),
                                        Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void failure(RetrofitError retrofitError) {
                                Log.i(TAG, String.format("guess, retrofitResponse=%1$s, body=%2$s, kind=%3$s", retrofitError.getResponse(), retrofitError.getBody(), retrofitError.getKind()));
                            }
                        });

            }
        });

        BattleshipGameCollection.getInstance().setOnGameSetChangedListener(new BattleshipGameCollection.OnGameSetChangedListener() {
            @Override
            public void onGameSetChanged() {
                _gameMenuFragment.refreshGameMenu();
            }
        });
    }

    private void refreshBoard() {
        BattleshipGameCollection.getInstance().getCurrentGame().setOnTurnChangedListener(new BattleshipGameModel.OnTurnChangedListener() {
            @Override
            public void OnTurnChanged() {
                _gamePlayFragment.refreshPlayersGrids();
            }
        });

        PlayerId playerId = new PlayerId(BattleshipGameCollection.getInstance().getCurrentGame().getPlayerId());
        battleshipService.requestBoard(BattleshipGameCollection.getInstance().getCurrentGame().getGameId(), playerId, new Callback<PlayerBoardResponse>() {
            @Override
            public void success(PlayerBoardResponse playerBoardResponse, Response response) {
                BattleshipGameCollection.getInstance().getCurrentGame().setBoards(playerBoardResponse);
                Log.i(TAG, "board received!");
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.e(TAG, "requestBoard, retrofitError=" + retrofitError.getResponse());
            }
        });
    }
    private AsyncTask pollCurrentTurn;
    private void pollCurrentTurn() {
       pollCurrentTurn = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                while (!isCancelled()) {
                    PlayerId playerId = new PlayerId(BattleshipGameCollection.getInstance().getCurrentGame().getPlayerId());
                    battleshipService.determineTurn(BattleshipGameCollection.getInstance().getCurrentGame().getGameId(),
                            playerId,
                            new Callback<CurrentTurnResponse>() {
                        @Override
                        public void success(CurrentTurnResponse currentTurnResponse, Response response) {
                            BattleshipGameCollection.getInstance().getCurrentGame().setCurrentTurn(currentTurnResponse);
                        }

                        @Override
                        public void failure(RetrofitError retrofitError) {
                            Log.i(TAG, "pollCurrentTurn, retrofitError=" + retrofitError.getResponse());
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            pollCurrentTurn.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);
        else
            pollCurrentTurn.execute((Void[])null);
    }


    private void pollGameList() { //final BattleshipService service, final int start, final int num) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                while(!isCancelled()) {
                    battleshipService.gamesList(new Callback<List<NetworkGame>>() {
                        @Override
                        public void success(List<NetworkGame> networkGames, Response response) {
                            BattleshipGameCollection.getInstance().addBattleshipGameModels(networkGames);
                        }

                        @Override
                        public void failure(RetrofitError retrofitError) {
                            Log.i(TAG, "pollGameList, retrofitError=" + retrofitError.getResponse());
                        }
                    });
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }
        }.execute();
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
            pollGameList();
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
        void guess(@Path("id") String id, @Body Guess guess, Callback<GuessResponse> guessResponseCallback);

        @POST("/api/games/{id}/status")
        void determineTurn(@Path("id") String id, @Body PlayerId playerId, Callback<CurrentTurnResponse> currentTurnResponseCallback);

        @POST("/api/games/{id}/board")
        void requestBoard(@Path("id") String id, @Body PlayerId playerId, Callback<PlayerBoardResponse> playerBoardResponseCallback);
    }
}