package com.example.tsundoku;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.google.gson.Gson;

public class BookDetailsActivity extends AppCompatActivity {

    private TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_book_details);

        Gson gson = new Gson();
        Book detailsBook = gson.fromJson(getIntent().getStringExtra("book"), Book.class);

        title = findViewById(R.id.details_book_title);
        title.setText(detailsBook.getTitle());
    }
}
