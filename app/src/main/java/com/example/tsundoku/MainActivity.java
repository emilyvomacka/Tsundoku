package com.example.tsundoku;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import util.BookApi;

import static com.example.tsundoku.Constants.BOOKS_TOKEN;

public class MainActivity extends AppCompatActivity implements MyAdapter.OnBookListener {


    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    private CollectionReference collectionReference = db.collection("Unread Books");
    private RequestQueue requestQueue;
    private String currentUserId;
    private String currentUserName;

    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton addBookButton;
    private ArrayList<Book> bookList;
    private RecyclerView recyclerView;
    private MyAdapter myAdapter;

    private final int REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        if (savedInstanceState == null) {
//            startActivity(new Intent(this, LoginActivity.class));
//        }

        addBookButton = findViewById(R.id.fab);
        bookList = new ArrayList<>();
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        addBookButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, BarcodeCamera.class);
            startActivityForResult(intent, REQUEST_CODE);
        });


//
//        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
//        bottomNavigationView.inflateMenu(R.menu.bottom_nav_menu);
//
//        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
//            switch (item.getItemId()) {
//                case R.id.library:
//                    Toast.makeText(MainActivity.this, "My Library", Toast.LENGTH_SHORT).show();
//                    break;
//                case R.id.stats:
//                    Toast.makeText(MainActivity.this, "Shelf Stats", Toast.LENGTH_SHORT).show();
//                    break;
//                case R.id.about:
//                    Toast.makeText(MainActivity.this, "About", Toast.LENGTH_SHORT).show();
//                    break;
//            }
//            return true;
//        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        firebaseAuth = FirebaseAuth.getInstance();

        if (BookApi.getInstance() != null) {
            currentUserId = BookApi.getInstance().getUserId();
            currentUserName = BookApi.getInstance().getUsername();
        }

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {

                } else {

                }
            }
        };

        user = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);

        bookList.clear();
        collectionReference.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot dbBooks) {
                if (!dbBooks.isEmpty()) {
                    for (QueryDocumentSnapshot doc : dbBooks) {
                            Book book = doc.toObject(Book.class);
                            bookList.add(book);
                            Log.d("DEBUG", book.getTitle());
                    }

                    myAdapter = new MyAdapter(MainActivity.this, bookList,
                            MainActivity.this);
                    recyclerView.setAdapter(myAdapter);
                    myAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        requestQueue = Volley.newRequestQueue(this);

        Log.d("DEBUG", "requestCode is " + requestCode + ", REQUEST_CODE is " + REQUEST_CODE);

        if (requestCode == REQUEST_CODE) {
            String enteredIsbn = data.getStringExtra("scannedIsbn");
            Log.d("DEBUG", "entered ISBN is " + enteredIsbn);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                "https://www.googleapis.com/books/v1/volumes?q=+isbn:" + enteredIsbn +
                        "&key=" + BOOKS_TOKEN, null,

                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            //parsing book
                            JSONArray bookArray = response.getJSONArray("items");
                            JSONObject bookJSON = bookArray.getJSONObject(0);
                            JSONObject volumeInfo = bookJSON.getJSONObject("volumeInfo");
                            String parsedTitle = volumeInfo.getString("title");
                            JSONArray authorsArray = volumeInfo.getJSONArray("authors");
                            String parsedAuthor = authorsArray.getString(0);
                            String parsedDescription = volumeInfo.getString("description");
                            JSONObject imageLinks = volumeInfo.getJSONObject("imageLinks");
                            String parsedImageUrl = imageLinks.getString("thumbnail");
                            String parsedHttpsImageUrl = parsedImageUrl.substring(0, 4) + "s" + parsedImageUrl.substring(4);

                            //Adding book
                            Book newBook = new Book(parsedTitle, parsedAuthor, parsedDescription,
                                    parsedHttpsImageUrl, new Timestamp(new Date()),
                                    currentUserId, currentUserName);

                            collectionReference.add(newBook)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {

                                        //TODO could move to MainActivity here
                                        Toast.makeText(MainActivity.this, parsedTitle +
                                                "Success! " + parsedTitle + " added to library",
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

    @Override
    public void onBookClick(int position) {
        Intent intent = new Intent(MainActivity.this, BookDetailsActivity.class);
        Gson gson = new Gson();
        String detailsBook = gson.toJson(bookList.get(position));
        intent.putExtra("book", detailsBook);
        startActivity(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}


