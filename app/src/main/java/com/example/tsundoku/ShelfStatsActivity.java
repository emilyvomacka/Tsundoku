package com.example.tsundoku;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ShelfStatsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shelf_stats);


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.library:
                    startActivity(new Intent(ShelfStatsActivity.this, MainActivity.class));
                    finish();
                    break;
                case R.id.stats:

                    break;
                case R.id.about:
                    Toast.makeText(ShelfStatsActivity.this, "About", Toast.LENGTH_SHORT).show();
                    break;
            }
            return true;
        });
    }
}
