package com.example.tsundoku;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Main";
    private EditText enterTitle;
    private EditText enterDesc;
    private Button saveButton;
    private Button showButton;
    private TextView recBook, recDesc;

    //Keys
    public static final String KEY_TITLE = "title";
    public static final String KEY_DESC = "description";

    //Connection to Firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference bookRef = db.collection("Unread Books").document("First Book");

    RecyclerView recyclerView;

    String s1[], s2[];
    int images[] = { R.drawable.book, R.drawable.book, R.drawable.book, R.drawable.book,
            R.drawable.book, R.drawable.book };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        saveButton = findViewById(R.id.save_button);
        recyclerView = findViewById(R.id.recyclerView);
        enterTitle = findViewById(R.id.edit_text_title);
        enterDesc = findViewById(R.id.edit_text_desc);
        showButton = findViewById(R.id.show_data);
        recBook = findViewById(R.id.rec_book);
        recDesc = findViewById(R.id.rec_desc);

        s1 = getResources().getStringArray(R.array.programming_languages);
        s2 = getResources().getStringArray(R.array.desc);

        MyAdapter myAdapter = new MyAdapter(this, s1, s2, images);
        recyclerView.setAdapter(myAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //from firestore tutorial
        db.collection("Unread Books")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        List<String> unreadBooks = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            if (doc.get("title") != null) {
                                unreadBooks.add(doc.getString("title"));
                            }
                        }
                        Log.d(TAG, "Unread books: " + unreadBooks);
                    }
                });
        //end firestore tutorial

        showButton.setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                        bookRef.get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                       if (documentSnapshot.exists()) {
                                            String title = documentSnapshot.getString(KEY_TITLE);
                                            String desc = documentSnapshot.getString(KEY_DESC);
                                            recBook.setText(title);
                                            recDesc.setText(desc);
                                       } else {
                                           Toast.makeText(MainActivity.this, "No data exists",
                                           Toast.LENGTH_LONG);
                                       }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: " + e.toString());
                                    }
                                });
                  }
              }
        );

        saveButton.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                    String title = enterTitle.getText().toString().trim();
                    String description = enterDesc.getText().toString().trim();

                    Map<String, Object> data = new HashMap<>();
                    data.put(KEY_TITLE, title);
                    data.put(KEY_DESC, description);

                    db.collection("Unread Books")
                            .document("Second Book")
                            .set(data)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(MainActivity.this,  "Success!!", Toast.LENGTH_LONG).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure: " + e.toString());
                                }
                            });
              }
        });
    }

}
