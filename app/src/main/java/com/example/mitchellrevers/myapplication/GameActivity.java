package com.example.mitchellrevers.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Map;

public class GameActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private Query gameRef;

    private List<Long> myBoardSetup;
    private List<Long> myBoardClicks;
    private List<Long> theirBoardSetup;
    private List<Long> theirBoardClicks;
    private boolean currentTurn;
    private boolean sendingUser;
    private boolean gameOver = false;

    private TextView turnText;
    private TableLayout myTable;
    private TableLayout otherTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        sendingUser = getIntent().getBooleanExtra("SENDING_USER", false);

        turnText = (TextView) findViewById(R.id.currentTurn);
        myTable = (TableLayout) findViewById(R.id.myBoardLayout);
        otherTable = (TableLayout) findViewById(R.id.theirBoardLayout);

        if (sendingUser) {
            turnText.setText("Your turn!");
        }
        else {
            turnText.setText("Waiting for other player...");
        }

        // Lookup board setup
        int gameCode = getIntent().getIntExtra("GAME_CODE", -99999999);
        gameRef = mDatabase.orderByChild("gameCode").equalTo(gameCode);
        gameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
                Map<String, Object> post = (Map<String, Object>) snapshot.getValue();
                if (sendingUser) {
                    myBoardSetup = (List<Long>) post.get("sendingBoardSetup");
                    myBoardClicks = (List<Long>) post.get("sendingBoardClicks");
                    theirBoardSetup = (List<Long>) post.get("receivingBoardSetup");
                    theirBoardClicks = (List<Long>) post.get("receivingBoardClicks");
                }
                else {
                    myBoardSetup = (List<Long>) post.get("receivingBoardSetup");
                    myBoardClicks = (List<Long>) post.get("receivingBoardClicks");
                    theirBoardSetup = (List<Long>) post.get("sendingBoardSetup");
                    theirBoardClicks = (List<Long>) post.get("sendingBoardClicks");
                }
                currentTurn = (Boolean) post.get("currentTurn");

                // Iterate through rows to set up board
                for (int i = 0; i < myTable.getChildCount(); i++) {
                    View view = myTable.getChildAt(i);
                    if (view instanceof TableRow) {
                        TableRow row = (TableRow) view;
                        for (int j = 0; j < row.getChildCount(); j++) {
                            Button button = (Button) row.getChildAt(j);
                            button.setTag(BattleshipGame.BOARD_WIDTH * i + j);
                        }
                    }
                }

                drawBoard(myTable, myBoardSetup, myBoardClicks);

                // Iterate through rows to set up board
                for (int i = 0; i < otherTable.getChildCount(); i++) {
                    View view = otherTable.getChildAt(i);
                    if (view instanceof TableRow) {
                        TableRow row = (TableRow) view;
                        for (int j = 0; j < row.getChildCount(); j++) {
                            Button button = (Button) row.getChildAt(j);
                            button.setTag(BattleshipGame.BOARD_WIDTH * i + j);
                            if (theirBoardSetup.get(BattleshipGame.BOARD_WIDTH * i + j).intValue() == 1) {
                                button.setBackgroundResource(R.drawable.cell_highlight);
                            }
                        }
                    }
                }

                drawBoard(otherTable, theirBoardSetup, theirBoardClicks);
            }

            @Override
            public void onChildRemoved(DataSnapshot snapshot) {
                if (!gameOver) {
                    Toast.makeText(getApplication(), "Player disconnected", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), LobbyActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, String str) {
                Map<String, Object> post = (Map<String, Object>) snapshot.getValue();
                if (sendingUser) {
                    myBoardClicks = (List<Long>) post.get("sendingBoardClicks");
                    theirBoardClicks = (List<Long>) post.get("receivingBoardClicks");
                }
                else {
                    myBoardClicks = (List<Long>) post.get("receivingBoardClicks");
                    theirBoardClicks = (List<Long>) post.get("sendingBoardClicks");
                }
                currentTurn = (Boolean) post.get("currentTurn");
                if (sendingUser) {
                    if (currentTurn) {
                        turnText.setText("Your turn!");
                    }
                    else {
                        turnText.setText("Waiting for other player...");
                    }
                }
                else {
                    if (!currentTurn) {
                        turnText.setText("Your turn!");
                    }
                    else {
                        turnText.setText("Waiting for other player...");
                    }
                }

                drawBoard(myTable, myBoardSetup, myBoardClicks);
                drawBoard(otherTable, theirBoardSetup, theirBoardClicks);
            }

            @Override
            public void onChildMoved(DataSnapshot snapshot, String str) {
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        gameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    child.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        this.finish();
    }

    public void drawBoard(TableLayout table, List<Long> setup, List<Long> clicks) {
        // Iterate through rows to set up board
        int total = 0;
        for (int i = 0; i < table.getChildCount(); i++) {
            View view = table.getChildAt(i);
            if (view instanceof TableRow) {
                TableRow row = (TableRow) view;
                for (int j = 0; j < row.getChildCount(); j++) {
                    Button button = (Button) row.getChildAt(j);
                    int cellNumber = BattleshipGame.BOARD_WIDTH * i + j;
                    if (clicks.get(cellNumber) == 1) {
                        if (setup.get(cellNumber).intValue() == 1) {
                            button.setText("X");
                            total++;
                        }
                        else {
                            button.setText("•");
                        }
                    }
                }
            }
        }

        // Check if game has been completed
        if (total == BattleshipGame.EXPECTED_TOTAL) {
            Button quitButton = findViewById(R.id.quitButton);
            quitButton.setVisibility(View.VISIBLE);
            if (table.equals(findViewById(R.id.myBoardLayout))) {
                Toast.makeText(getApplicationContext(), "You won!", Toast.LENGTH_SHORT).show();
                turnText.setText("You won -");
            }
            else {
                Toast.makeText(getApplicationContext(), "You lost", Toast.LENGTH_SHORT).show();
                turnText.setText("You lost -");
            }
            gameOver = true;
        }
    }

    public void spaceClick(View view) {
        if (!gameOver && (sendingUser && currentTurn || !sendingUser && !currentTurn)) {
            int cellNumber = (int) view.getTag();
            if (myBoardClicks.get(cellNumber) == 0) {
                myBoardClicks.set(cellNumber, 1L);
                gameRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            if (sendingUser) {
                                child.getRef().child("sendingBoardClicks").setValue(myBoardClicks);
                            }
                            else {
                                child.getRef().child("receivingBoardClicks").setValue(myBoardClicks);
                            }

                            currentTurn = !currentTurn;
                            child.getRef().child("currentTurn").setValue(currentTurn);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
        }
    }

    public void quitButtonClick(View view) {
        startActivity(new Intent(this, LobbyActivity.class));
        gameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    child.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        this.finish();
    }
}
