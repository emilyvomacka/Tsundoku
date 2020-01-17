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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.example.tsundoku.Constants.BOOKS_TOKEN;

public class MainActivity extends AppCompatActivity implements NavigationHost {

    private FloatingActionButton addBookButton;
    public List<Book> bookList;
    private RecyclerView recyclerView;
    private MyAdapter myAdapter;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference unreadBooks = db.collection("Unread Books");
    private RequestQueue requestQueue;
    private BottomNavigationView bottomNavigationView;

    private final int REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, new LoginFragment())
                    .commit();
        }

//        addBookButton = findViewById(R.id.fab);
//        bookList = new ArrayList<>();
//        recyclerView = findViewById(R.id.recyclerView);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//
//        addBookButton.setOnClickListener(v -> {
//            Intent intent = new Intent(MainActivity.this, BarcodeCamera.class);
//            startActivityForResult(intent, REQUEST_CODE);
//        });
//
//        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
////        bottomNavigationView.inflateMenu(R.menu.bottom_nav_menu);
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


//    @Override
//    protected void onStart() {
//
//        super.onStart();
//            bookList.clear();
//            unreadBooks.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                @Override
//                public void onSuccess(QuerySnapshot dbBooks) {
//                    if (!dbBooks.isEmpty()) {
//                        for (QueryDocumentSnapshot doc : dbBooks) {
//                                Book book = doc.toObject(Book.class);
//                                bookList.add(book);
//                                Log.d("DEBUG", book.getTitle());
//                        }
//
//                        myAdapter = new MyAdapter(MainActivity.this, bookList);
//                        recyclerView.setAdapter(myAdapter);
////                        myAdapter.notifyDataSetChanged();
//                    }
//                }
//        });
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        requestQueue = Volley.newRequestQueue(this);

        Log.d("DEBUG", "requestCode is " + requestCode + ", REQUEST_CODE is " + REQUEST_CODE);

        if (requestCode == REQUEST_CODE) {

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
                            Log.d("DEBUG", "received response " + response);

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

                            Book newBook = new Book(parsedTitle, parsedAuthor, parsedDescription,
                                    parsedHttpsImageUrl, new Date());

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


    public void navigateTo(Fragment fragment, boolean addToBackstack) {
        FragmentTransaction transaction =
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, fragment);

        if (addToBackstack) {
            transaction.addToBackStack(null);
        }

        transaction.commit();
    }

//    public void showDetails(View view) {
//        Intent intent = new Intent(this, DisplayMessageActivity.class);
//        EditText editText = (EditText) findViewById(R.id.editText);
//        String message = editText.getText().toString();
//        intent.putExtra();
//        startActivity(intent);
//    }
}


