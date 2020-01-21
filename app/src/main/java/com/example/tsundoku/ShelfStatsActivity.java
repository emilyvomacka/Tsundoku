package com.example.tsundoku;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import util.BookApi;

public class ShelfStatsActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    private CollectionReference collectionReference = db.collection("Unread Books");
    private int unreadBookQuantity;
    private int unreadPageQuantity;
    private TextView statsOne, statsTwo, statsThree;

    private String currentUserId;
    private String currentUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shelf_stats);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.library:
                    startActivity(new Intent(ShelfStatsActivity.this, MainActivity.class));
                    finish();
                    break;
                case R.id.stats:

                    break;
                case R.id.maps:
                    startActivity(new Intent(ShelfStatsActivity.this, BookstoresActivity.class));
                    finish();
                    break;
            }
            return true;
        });

    unreadBookQuantity = 0;
    unreadPageQuantity = 0;

    firebaseAuth = FirebaseAuth.getInstance();
    user = firebaseAuth.getCurrentUser();

    authStateListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            user = firebaseAuth.getCurrentUser();
                if (user != null) {

                }
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.main_settings:

                break;
            case R.id.main_logout:
                if (user != null && firebaseAuth != null) {
                    firebaseAuth.signOut();
                    startActivity(new Intent(ShelfStatsActivity.this, LoginActivity.class));
                    finish();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (BookApi.getInstance() != null) {
            currentUserId = BookApi.getInstance().getUserId();
            currentUserName = BookApi.getInstance().getUsername();
        }

        statsOne = findViewById(R.id.stats_1);
        statsTwo = findViewById(R.id.stats_2);
        statsThree = findViewById(R.id.stats_3);

        collectionReference.whereEqualTo("userId", currentUserId)
                .whereEqualTo("status", "unread")
                .get()
            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot dbBooks) {
                    if (!dbBooks.isEmpty()) {
                        for (QueryDocumentSnapshot doc : dbBooks) {
                            Book book = doc.toObject(Book.class);
                            unreadBookQuantity += 1;
                            unreadPageQuantity += book.getPageCount();
                        }
                    }

                    statsOne.setText(unreadBookQuantity == 0 ? "You have no unread books."
                             : "You have " +
                            unreadBookQuantity + " unread books. We can work with this.");

                    statsTwo.setText(unreadBookQuantity == 0 ? "Either you're a reading machine or you're not being honest." : "That's " +
                            unreadPageQuantity + " pages to go.");

                    statsThree.setText("Tsundoku 2.0 will feature more insights into " +
                            "what you like to read, and projections of how quickly " +
                            "you'll get through your stack based on your average reading " +
                            "speed.");
                }
            });
    }
}
