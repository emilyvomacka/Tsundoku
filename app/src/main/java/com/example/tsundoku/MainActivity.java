package com.example.tsundoku;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Button addBookButton;

    //Connection to Firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();


    RecyclerView recyclerView;

    String s1[], s2[];
    int images[] = { R.drawable.book, R.drawable.book, R.drawable.book, R.drawable.book,
            R.drawable.book, R.drawable.book };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        addBookButton = findViewById(R.id.button_add_book);

        s1 = getResources().getStringArray(R.array.programming_languages);
        s2 = getResources().getStringArray(R.array.desc);

        MyAdapter myAdapter = new MyAdapter(this, s1, s2, images);
        recyclerView.setAdapter(myAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        addBookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddBook.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //updating snapshot from firestore tutorial
        db.collection("Unread Books")
                .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot dbBooks,
                                        @Nullable FirebaseFirestoreException e) {

                        if (e != null) {
                            Toast.makeText(MainActivity.this, "Something Went Wrong",
                                    Toast.LENGTH_LONG).show();
                            Log.w("Debug", "Listen failed.", e);
                            return;
                        }

                        List<String> unreadBooks = new ArrayList<>();

                        for (QueryDocumentSnapshot doc : dbBooks) {
                            if (doc.get("title") != null) {
                                unreadBooks.add(doc.getString("title"));
                            }
                        }

                        Toast.makeText(MainActivity.this, "Snapshot created",
                                Toast.LENGTH_LONG).show();
                    }
                });
        //end firestore tutorial
    }


}
