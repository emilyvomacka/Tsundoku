package com.example.tsundoku;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


public class BookShelfFragment extends Fragment {

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.book_shelf_fragment, container, false);

        // Set up the RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        //get books
        bookList = new ArrayList<>();
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

                        myAdapter = new MyAdapter(getContext(), bookList);
                        recyclerView.setAdapter(myAdapter);
                        myAdapter.notifyDataSetChanged();
                    }
                }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
//        addBookButton = findViewById(R.id.fab);
//
//        addBookButton.setOnClickListener(v -> {
//            Intent intent = new Intent(getContext(), BarcodeCamera.class);
//            startActivityForResult(intent, REQUEST_CODE);
//        });
//
//        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
////        bottomNavigationView.inflateMenu(R.menu.bottom_nav_menu);
//
//        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
//            switch (item.getItemId()) {
//                case R.id.library:
//                    Toast.makeText(getContext(), "My Library", Toast.LENGTH_SHORT).show();
//                    break;
//                case R.id.stats:
//                    Toast.makeText(getContext(), "Shelf Stats", Toast.LENGTH_SHORT).show();
//                    break;
//                case R.id.about:
//                    Toast.makeText(getContext(), "About", Toast.LENGTH_SHORT).show();
//                    break;
//            }
//            return true;
//        });
    }
}
