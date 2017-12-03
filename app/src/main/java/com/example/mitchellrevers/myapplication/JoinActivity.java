package com.example.mitchellrevers.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JoinActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabase;
    private String mUserId;
    private List<String> availableGames = new ArrayList<>();
    private ListView listView;
    private FirebaseListAdapter myAdapter;

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        listView = findViewById(R.id.listView);

        if (mFirebaseUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
        }
        else {
            mUserId = mFirebaseUser.getUid();
            myAdapter = new FirebaseListAdapter<BattleshipGame>(this, BattleshipGame.class, R.layout.game_row, mDatabase) {
                @Override
                protected void populateView(View view, BattleshipGame model, int i) {
                    TextView creator = (TextView) view.findViewById(R.id.textViewCode);
                    creator.setText(model.getSendingUser());

                    TextView date = (TextView) view.findViewById(R.id.textViewDate);
                    date.setText("Created " + model.getDateCreated());

                    Button button = (Button) view.findViewById(R.id.joinButton);
                    button.setTag(model.getGameCode());
                    if (model.getReceivingUser() != null || model.getSendingUser().equals(mFirebaseUser.getEmail())) {
                        button.setEnabled(false);
                        button.setBackgroundColor(Color.RED);
                    }
                }
            };

            final ListView lv = (ListView) findViewById(R.id.listView);
            lv.setAdapter(myAdapter);
        }
    }

    public void joinGame(View view) {
        final int gameCode = (int) view.getTag();

        // set receiving user to you
        Query ref = mDatabase.orderByChild("gameCode").equalTo(gameCode);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Map<String, Object> post = (Map<String, Object>) child.getValue();
                    if (post.get("receivingUser") == null) {
                        child.getRef().child("receivingUser").setValue(mFirebaseUser.getEmail());
                        startGame(gameCode);
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Game is already full", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        this.finish();
    }

    public void startGame(int gameCode) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("GAME_CODE", gameCode);
        intent.putExtra("SENDING_USER", false);
        startActivity(intent);
        this.finish();
    }

    public void availableGamesClick(MenuItem item) {
    }

    public void homeClick(MenuItem item) {
        startActivity(new Intent(this, LobbyActivity.class));
        overridePendingTransition(0, 0);
        this.finish();
    }
}
