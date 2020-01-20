package com.example.tsundoku;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.format.DateUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.Date;

public class BookDetailsActivity extends AppCompatActivity {

    private TextView title, author, desc, dateAdded;
    private ImageView coverImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_book_details);

        Gson gson = new Gson();
        Book detailsBook = gson.fromJson(getIntent().getStringExtra("book"), Book.class);

        title = findViewById(R.id.details_book_title);
        title.setText(detailsBook.getTitle());

        author = findViewById(R.id.details_book_author);
        author.setText(detailsBook.getAuthor());

        desc = findViewById(R.id.details_book_desc);
        desc.setText(detailsBook.getDescription());

        coverImage = findViewById(R.id.details_book_image);
        String imageUrl = detailsBook.getImageUrl();

        //Use Picasso to download and show image
        Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.book)
                .fit()
                .into(this.coverImage);

//        String timeAgo = (String) DateUtils.getRelativeTimeSpanString
//                        (detailsBook.getDateAdded().getSeconds() * 1000);


        dateAdded = findViewById(R.id.details_book_date_added);
        String timeAgo =
                (String) DateUtils.getRelativeTimeSpanString(detailsBook.getTimeAdded().getSeconds() * 1000);
        dateAdded.setText(timeAgo);
    }
}
