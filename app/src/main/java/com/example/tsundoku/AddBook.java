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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddBook extends AppCompatActivity {

    private EditText enterTitle;
    private EditText enterDesc;
    private Button saveButton;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Unread Books");

    public static final String KEY_TITLE = "title";
    public static final String KEY_DESC = "description";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        enterTitle = findViewById(R.id.edit_text_title);
        enterDesc = findViewById(R.id.edit_text_desc);
        saveButton = findViewById(R.id.save_button);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("DEBUG", "entered onClick Add Book method");
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
