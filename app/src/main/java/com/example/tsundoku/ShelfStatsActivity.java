package com.example.tsundoku;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

    private CollectionReference unreadBooks;
    private int unreadBookQuantity;
    private int unreadPageQuantity;
    private TextView statsOne, statsTwo, statsThree;

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
                    Toast.makeText(ShelfStatsActivity.this, "Maps", Toast.LENGTH_SHORT).show();
                    break;
            }
            return true;
        });

    unreadBookQuantity = 0;
    unreadPageQuantity = 0;

    firebaseAuth = FirebaseAuth.getInstance();
    user = firebaseAuth.getCurrentUser();
    Log.d("DEBUG", BookApi.getInstance().getUsername());

    unreadBooks.whereEqualTo("userId", BookApi.getInstance().getUserId())
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
                }
            });

    statsOne = findViewById(R.id.stats_1);
    statsTwo = findViewById(R.id.stats_2);
    statsThree = findViewById(R.id.stats_3);

    statsOne.setText(unreadBookQuantity == 0 ? "You have no unread books" : "You have " +
            unreadBookQuantity + " unread books.");

    }


}
