package com.example.tsundoku;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.renderscript.ScriptGroup;
import android.util.Log;
import android.view.MenuItem;
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
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static com.example.tsundoku.Constants.BOOKS_MAPS_TOKEN;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton addBookButton;
    private List<Book> bookList;
    private RecyclerView recyclerView;
    private MyAdapter myAdapter;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference unreadBooks = db.collection("Unread Books");
    private RequestQueue requestQueue;
    private BottomNavigationView bottomNavigationView;

    private final int REQUEST_CODE = 2;

    private boolean runningQOrLater =
            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q;
    private @Nullable ScriptGroup.Binding binding;
    private int REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33;
    private int REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34;
    private int REQUEST_TURN_DEVICE_LOCATION_ON = 29;
    private int LOCATION_PERMISSION_INDEX = 0;
    private int BACKGROUND_LOCATION_PERMISSION_INDEX = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addBookButton = findViewById(R.id.fab);
        bookList = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        addBookButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, BarcodeCamera.class);
            startActivityForResult(intent, REQUEST_CODE);
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
//        bottomNavigationView.inflateMenu(R.menu.bottom_nav_menu);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.library:
                    Toast.makeText(MainActivity.this, "My Library", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.stats:
                    Toast.makeText(MainActivity.this, "Shelf Stats", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.about:
                    Toast.makeText(MainActivity.this, "About", Toast.LENGTH_SHORT).show();
                    break;
            }
            return true;
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
//                      myAdapter.notifyDataSetChanged();
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

            Log.d("DEBUG", "entered onActivityResult method in Main");

            String enteredIsbn = data.getStringExtra("scannedIsbn");
            Log.d("DEBUG", "entered ISBN is " + enteredIsbn);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, "https://www.googleapis.com/books/v1/volumes?q=+isbn:" + enteredIsbn +
                            "&key=" + BOOKS_MAPS_TOKEN, null,

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

                            String parsedHttpsImageUrl = parsedImageUrl.substring(0, 4) + "s" + parsedImageUrl.substring(4);
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
                    error -> Log.d("DEBUG", "onErrorResponse: " + error.getMessage()));

            requestQueue.add(jsonObjectRequest);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.d("DEBUG", "onRequestPermissionResult")

        if (grantResults.isEmpty() || grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED || (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE && grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED))
        {
            Toast.makeText(this, "Location permissions denied", Toast.LENGTH_LONG).show();
        } else {
            checkDeviceLocationSettingsAndStartGeofence();
        }
    }

    @TargetApi(29)
        private boolean foregroundAndBackgroundLocationPermissionApproved() {
            boolean foregroundLocationApproved = (
                    PackageManager.PERMISSION_GRANTED ==
                            ActivityCompat.checkSelfPermission(this,
                                    Manifest.permission.ACCESS_FINE_LOCATION));
            boolean backgroundPermissionApproved = if (runningQOrLater) {
                ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                );
            } else {
                "true"
            }
            return foregroundLocationApproved && backgroundPermissionApproved;
        }

        @TargetApi(29)
        private boolean requestForegroundAndBackgroundLocationPermissions() {
            if (foregroundAndBackgroundLocationPermissionApproved()) {
                return;
            } else {
                // Else request the permission
                // this provides the result[LOCATION_PERMISSION_INDEX]
                ArrayList<Boolean> permissionsArray = new ArrayList<Boolean>();
                permissionsArray = [Manifest.permission.ACCESS_FINE_LOCATION];
            }

            int resultCode = when {
                runningQOrLater -> {
                    // this provides the result[BACKGROUND_LOCATION_PERMISSION_INDEX]
                    permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
                }
                else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
            }

            Log.d("DEBUG", "Request foreground only location permission");
            ActivityCompat.requestPermissions(MainActivity.this, permissionsArray, resultCode);
        }

    private void checkDeviceLocationSettingsAndStartGeofence(resolve:Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val settingsClient = LocationServices.getSettingsClient(this)
        val locationSettingsResponseTask =
                settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve){
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(MainActivity.this,
                    REQUEST_TURN_DEVICE_LOCATION_ON)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d("DEBUG", "Error geting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(
                        binding.activityMapsMain,
                        R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettingsAndStartGeofence()
                }.show()
            }
        }
        locationSettingsResponseTask.addOnSuccessListener {
            if ( it.isSuccessful ) {
                addGeofenceForClue()
            }
        }
    }


    }




