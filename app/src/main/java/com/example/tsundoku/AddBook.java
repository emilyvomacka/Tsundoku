package com.example.tsundoku;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.example.tsundoku.Constants.BOOKS_TOKEN;

public class AddBook extends AppCompatActivity {

    private EditText enterTitle;
    private EditText enterIsbn;
    private Button saveButton;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Unread Books");

    private RequestQueue requestQueue;

    public static final String KEY_TITLE = "title";
    public static final String KEY_DESC = "description";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        enterTitle = findViewById(R.id.edit_text_title);
        enterIsbn = findViewById(R.id.enter_isbn);
        saveButton = findViewById(R.id.save_button);

        requestQueue = Volley.newRequestQueue(this);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("DEBUG", "entered onClick Add Book method");

                String enteredIsbn = enterIsbn.getText().toString().trim();

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                        "https://www.googleapis.com/books/v1/volumes?q=+isbn:" + enteredIsbn +
                                "&key=" +
                        BOOKS_TOKEN, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    JSONArray bookArray = response.getJSONArray("items");
                                    JSONObject bookJSON = bookArray.getJSONObject(0);
                                    JSONObject volumeInfo = bookJSON.getJSONObject("volumeInfo");

                                    String parsedTitle = volumeInfo.getString("title");
                                    JSONArray authorsArray = volumeInfo.getJSONArray("authors");
                                    String parsedAuthor = authorsArray.getString(0);
                                    String parsedDescription = volumeInfo.getString("description");
                                    JSONObject imageLinks = volumeInfo.getJSONObject("imageLinks");
                                    String parsedImageUrl =
                                            imageLinks.getString("thumbnail");
                                    String parsedHttpsImageUrl =
                                            parsedImageUrl.substring(0, 3) + "s" + parsedImageUrl.substring(4, -1);

                                    Book newBook = new Book(parsedTitle, parsedAuthor,
                                            parsedDescription, parsedHttpsImageUrl);

                                    Log.d("DEBUG", "Author is: " + parsedAuthor);

                                    collectionReference.add(newBook)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    Toast.makeText(AddBook.this, "Success! Book added from AddBook",
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
        });
    }
}
