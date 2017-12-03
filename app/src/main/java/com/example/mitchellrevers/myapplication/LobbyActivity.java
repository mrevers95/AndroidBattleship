package com.example.mitchellrevers.myapplication;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;

public class LobbyActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabase;
    private String mUserId;

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        if (mFirebaseUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            this.finish();
        }
        else {
            mUserId = mFirebaseUser.getUid();
            TextView greeting = findViewById(R.id.greeting);
            greeting.setText("Currently signed in as\n" + mFirebaseUser.getEmail());
            mDatabase.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
                    Map<String, Object> post = (Map<String, Object>) snapshot.getValue();
                    if (post.get("receivingUser") != null && post.get("receivingUser").equals(mFirebaseUser.getEmail())) {
                        Toast.makeText(getApplicationContext(), "Added to game", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), GameActivity.class);
                        intent.putExtra("GAME_CODE", ((Long) post.get("gameCode")).intValue());
                        intent.putExtra("SENDING_USER", false);
                        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(),
                                0, intent,
                                PendingIntent.FLAG_CANCEL_CURRENT);
                        NotificationManager nm = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                        Notification notification = new Notification.Builder(getApplicationContext())
                                .setContentIntent(contentIntent)
                                .setContentTitle("Click to join game")
                                .setContentText((String) post.get("sendingUser"))
                                .setSmallIcon(R.drawable.battleship)
                                .setAutoCancel(true)
                                .build();
                        nm.notify(1, notification);
                    }
                }

                @Override
                public void onChildRemoved(DataSnapshot snapshot) {
                    Map<String, Object> post = (Map<String, Object>) snapshot.getValue();
                    if (post.get("receivingUser") != null && post.get("receivingUser").equals(mFirebaseUser.getEmail())) {
                        NotificationManager nm = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                        nm.cancel(1);
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                }

                @Override
                public void onChildChanged(DataSnapshot snapshot, String str) {
                }

                @Override
                public void onChildMoved(DataSnapshot snapshot, String str) {
                }
            });
        }
    }

    public void createGame(View view) {
        BattleshipGame game = new BattleshipGame(mFirebaseUser.getEmail());
        mDatabase.push().setValue(game);
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("GAME_CODE", game.getGameCode());
        intent.putExtra("SENDING_USER", true);
        startActivity(intent);
        this.finish();
    }

    public void signoutButtonClick(View view) {
        mFirebaseAuth.signOut();
        startActivity(new Intent(this, LobbyActivity.class));
        this.finish();
    }

    public void availableGamesClick(MenuItem item) {
        startActivity(new Intent(this, JoinActivity.class));
        overridePendingTransition(0, 0);
        this.finish();
    }

    public void homeClick(MenuItem item) {
    }

    public void inviteButtonClick(View view) {
        EditText inviteEmail = findViewById(R.id.inviteEmail);
        BattleshipGame game = new BattleshipGame(mFirebaseUser.getEmail());
        game.setReceivingUser(inviteEmail.getText().toString());
        mDatabase.push().setValue(game);
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("GAME_CODE", game.getGameCode());
        intent.putExtra("SENDING_USER", true);
        startActivity(intent);
        this.finish();
    }
}
