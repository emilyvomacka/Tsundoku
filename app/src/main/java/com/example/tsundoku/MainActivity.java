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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.example.tsundoku.Constants.BOOKS_TOKEN;

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


    private final int REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addBookButton = findViewById(R.id.button_add_book);
        bookList = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        addBookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BarcodeCamera.class);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });
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

        @Override
        protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            requestQueue = Volley.newRequestQueue(this);

            if (requestCode == REQUEST_CODE) {
                assert data != null;
                String message = data.getStringExtra("scannedIsbn");
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }

            Log.d("DEBUG", "entered onActivityResult method in Main");

            String enteredIsbn = data.getStringExtra("scannedIsbn");
            Log.d("DEBUG", "entered ISBN is " + enteredIsbn);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
            "https://www.googleapis.com/books/v1/volumes?q=+isbn:" + enteredIsbn +
                    "&key=" + BOOKS_TOKEN, null,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        Log.d("DEBUG", "received response");

                        JSONArray bookArray = response.getJSONArray("items");
                        Log.d("DEBUG", "bookArray is " + bookArray);

                        JSONObject bookJSON = bookArray.getJSONObject(0);
                        Log.d("DEBUG", "bookJSON is " + bookJSON);

                        JSONObject volumeInfo = bookJSON.getJSONObject("volumeInfo");
                        Log.d("DEBUG", "volumeInfo is " + volumeInfo);

                        String parsedTitle = volumeInfo.getString("title");
                        Log.d("DEBUG", "parsedTitle is " + parsedTitle);

                        JSONArray authorsArray = volumeInfo.getJSONArray("authors");
                        String parsedAuthor = authorsArray.getString(0);
                        Log.d("DEBUG", "parsedAuthor is " + parsedAuthor);

                        String parsedDescription = volumeInfo.getString("description");
                        Log.d("DEBUG", "parsedDescription is " + parsedDescription);

                        JSONObject imageLinks = volumeInfo.getJSONObject("imageLinks");
                        String parsedImageUrl = imageLinks.getString("thumbnail");
                        Log.d("DEBUG", "parsedImage Url is " + parsedImageUrl);

                        String parsedHttpsImageUrl =
                                parsedImageUrl.substring(0, 4) + "s" + parsedImageUrl.substring(4);
                        Log.d("DEBUG", "parsedHttpsImageUrl is " + parsedHttpsImageUrl);

                        Book newBook = new Book(parsedTitle, parsedAuthor, parsedDescription, parsedHttpsImageUrl);

                        unreadBooks.add(newBook)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        Toast.makeText(MainActivity.this, "Success! Book added " +
                                                        "from AddBook",
                                                Toast.LENGTH_LONG).show();
                                        Log.d("DEBUG", "Added book from AddBookClass!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("DEBUG", "onFailure: " + e.toString());
                                    }
                                });


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

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
    }


