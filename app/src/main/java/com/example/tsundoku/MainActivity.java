package com.example.tsundoku;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public static final String STATUS_KEY = "status";
    public static final String PRIORITY_KEY = "priority";

    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton addBookButton;
    private ArrayList<Book> bookList;
    private RecyclerView recyclerView;
    private MyAdapter myAdapter;

    private final int REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        addBookButton = findViewById(R.id.fab);
        bookList = new ArrayList<>();
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        addBookButton.setOnClickListener(v -> {
            if (user != null && firebaseAuth != null) {
                Intent intent = new Intent(MainActivity.this, BarcodeCamera.class);
                startActivityForResult(intent, REQUEST_CODE);
            } else {
                Toast.makeText(MainActivity.this, "You must be logged in to add to your library",
                        Toast.LENGTH_SHORT).show();
            }
        });


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.library:
                    break;
                case R.id.stats:
                    startActivity(new Intent(MainActivity.this, ShelfStatsActivity.class));
                    finish();
                    break;
                case R.id.maps:
                    Toast.makeText(MainActivity.this, "About", Toast.LENGTH_SHORT).show();
                    break;
            }
            return true;
        });
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
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
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
        collectionReference.whereEqualTo("userId", BookApi.getInstance().getUserId())
            .whereEqualTo("status", "unread")
            .get()
            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot dbBooks) {
                if (!dbBooks.isEmpty()) {
                    for (QueryDocumentSnapshot doc : dbBooks) {
                            Book book = doc.toObject(Book.class);
                            bookList.add(book);
                            Log.d("DEBUG", book.getTitle());
                    }

                    Collections.sort(bookList);

                    myAdapter = new MyAdapter(MainActivity.this, bookList,
                            MainActivity.this);
                    recyclerView.setAdapter(myAdapter);
                    new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);
                    myAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        requestQueue = Volley.newRequestQueue(this);

        if (requestCode == REQUEST_CODE) {
            String enteredIsbn = data.getStringExtra("scannedIsbn");

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
                            Integer pageCount = Integer.parseInt(volumeInfo.getString("pageCount"));
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
                                    currentUserId, currentUserName, false, "unread", pageCount);

                            collectionReference.add(newBook)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        bookList.add(newBook);
                                        Collections.sort(bookList);
                                        myAdapter.notifyDataSetChanged();
                                        Toast.makeText(MainActivity.this,
                                                "Success! " + parsedTitle + " added to library",
                                                Toast.LENGTH_LONG).show();
                                        Log.d("DEBUG", "Added book from AddBookClass!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(MainActivity.this, "This book could" +
                                            "not be added to your library. Get it anyway, we won't tell.",
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                        } catch (JSONException e) {
                            Toast.makeText(MainActivity.this, "This book could " +
                                "not be added to your library. Get it anyway, we won't tell.",
                                Toast.LENGTH_LONG).show();
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
    public void onLongBookClick(int position) {
        boolean currentPriority = bookList.get(position).getPriority();
        collectionReference.whereEqualTo("userId", BookApi.getInstance().getUserId())
                .whereEqualTo("title", bookList.get(position).getTitle())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot dbBooks) {
                        if (!dbBooks.isEmpty()) {
                            for (QueryDocumentSnapshot doc : dbBooks) {
                                DocumentReference bookRef = doc.getReference();
                                Map<String, Object> data = new HashMap<>();
                                data.put(PRIORITY_KEY, !currentPriority);
                                bookRef.update(data);
                            }
                        }
                    }
                });
        bookList.get(position).setPriority(!currentPriority);
        Collections.sort(bookList);
        myAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    ItemTouchHelper.SimpleCallback itemTouchHelperCallback =
        new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView,
                              @NonNull RecyclerView.ViewHolder viewHolder,
                              @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            //update status to "removed"
            String removedTitle = bookList.get(viewHolder.getAdapterPosition()).getTitle();
            collectionReference.whereEqualTo("userId", BookApi.getInstance().getUserId())
                    .whereEqualTo("title", removedTitle)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot dbBooks) {
                            if (!dbBooks.isEmpty()) {
                                for (QueryDocumentSnapshot doc : dbBooks) {
                                    DocumentReference bookRef = doc.getReference();
                                    Log.d("DEBUG", bookRef.toString());
                                    Map<String, Object> data = new HashMap<>();
                                    data.put(STATUS_KEY, "removed");
                                    bookRef.update(data);
                                }
                            }
                        }
                    });
            bookList.remove(viewHolder.getAdapterPosition());
            myAdapter.notifyDataSetChanged();
            Toast.makeText(getBaseContext(), removedTitle + " has been removed from your library"
                    , Toast.LENGTH_SHORT).show();
        }
    };
}


