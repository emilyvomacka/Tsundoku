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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button addBookButton;
    private List<Book> bookList;
    private RecyclerView recyclerView;
    private MyAdapter myAdapter;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference unreadBooks = db.collection("Unread Books");

    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestQueue = Volley.newRequestQueue(this);

        addBookButton = findViewById(R.id.button_add_book);
        bookList = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        addBookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddBook.class);
                startActivity(intent);
            }
        });



        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, "https://jsonplaceholder.typicode.com/todos/1", null,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d("DEBUG", "onResponse: " + response);
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("DEBUG", "onErrorResponse: " + error.getMessage());
                }
        });
        requestQueue.add(jsonObjectRequest);
    }

    @Override
    protected void onStart() {
        super.onStart();
            bookList.clear();
            unreadBooks.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot dbBooks) {
                    if (!dbBooks.isEmpty()) {
                        for (QueryDocumentSnapshot doc : dbBooks) {
                                Book book = doc.toObject(Book.class);
                                bookList.add(book);
                                Log.d("DEBUG", book.getTitle());
                        }

                        myAdapter = new MyAdapter(MainActivity.this, bookList);
                        recyclerView.setAdapter(myAdapter);
//                        myAdapter.notifyDataSetChanged();
                    }
                }

            });
        }
    }


