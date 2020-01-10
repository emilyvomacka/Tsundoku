package com.example.tsundoku;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AddBook extends AppCompatActivity {

    private EditText enterTitle;
    private EditText enterDesc;
    private Button saveButton;
    private RequestQueue requestQueue;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Unread Books");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        enterTitle = findViewById(R.id.edit_text_title);
        enterDesc = findViewById(R.id.edit_text_desc);
        saveButton = findViewById(R.id.save_button);
        requestQueue = Volley.newRequestQueue(this);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("DEBUG", "entered onClick Add Book method");

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, "https://jsonplaceholder.typicode.com/todos/1", null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                Log.d("DEBUG", "onResponse: " + response.getString("title"));
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

                String title = enterTitle.getText().toString().trim();
                String description = enterDesc.getText().toString().trim();

                Book book = new Book();
                book.setTitle(title);
                book.setDescription(description);

                collectionReference.add(book);
//                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                        @Override
//                        public void onSuccess(DocumentReference documentReference) {
//                            Toast.makeText(AddBook.this, "Success! Book added from AddBook",
//                                    Toast.LENGTH_LONG).show();
//                            Log.d("DEBUG", "Added book from AddBookClass!");
//                        }
//                    })
//                        .addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                Log.d("DEBUG", "onFailure: " + e.toString());
//                            }
//                    });
            }
        });
    }
}
