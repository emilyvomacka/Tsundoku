package com.example.tsundoku;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import android.text.format.DateUtils;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    Context context;
    private ArrayList<Book> bookList;
    private OnBookListener mOnBookListener;

    public MyAdapter(Context ct, ArrayList<Book> bookList, OnBookListener onBookListener) {
        this.context = ct;
        this.bookList = bookList;
        this.mOnBookListener = onBookListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.my_row, parent, false);

        return new MyViewHolder(view, context, mOnBookListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Book book = bookList.get(position);
        String imageUrl;

        holder.title.setText(book.getTitle());

//        String timeAgo = (String) DateUtils.getRelativeTimeSpanString(book.getDateAdded().getSeconds() * 1000);
        holder.dateAdded.setText("today");

        imageUrl = book.getImageUrl();

        Log.d("DEBUG",
                "In Picasso image load, title is " + book.getTitle() + ", image url is " + book.getImageUrl() );

        //Use Picasso to download and show image
        Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.book)
                .fit()
                .into(holder.coverImage);
    }

    @Override
    public int getItemCount() {

        return bookList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView title, dateAdded;
        ImageView coverImage;
        OnBookListener onBookListener;

        public MyViewHolder(@NonNull View itemView, Context ct, OnBookListener onBookListener) {
            super(itemView);
            context = ct;
            coverImage = itemView.findViewById(R.id.mdcImageView);
            title = itemView.findViewById(R.id.mdcTitle);
            dateAdded = itemView.findViewById(R.id.mdcDateAdded);
            this.onBookListener = onBookListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onBookListener.onBookClick(getAdapterPosition());
        }
    }

    public interface OnBookListener{
        void onBookClick(int position);
    }
}
