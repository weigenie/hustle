package com.example.hustle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Timer;

public class StatsActivity extends AppCompatActivity {

    TextView text;
    static Long elapsed;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        elapsed = TimerActivity.totalTimeElapsed;
        text =  (TextView) findViewById(R.id.text_durationDisplay);
        long hours = elapsed.longValue() / 60;
        long minutes = elapsed.longValue()- hours * 60;

        text.setText(String.format("%dH %02dMINS", hours, minutes));

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.bottom_nav);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_timer:
                        Intent a = new Intent(StatsActivity.this,TimerActivity.class);
                        startActivity(a);
                        break;
                    case R.id.nav_todo:
                        Intent b = new Intent(StatsActivity.this,TodoActivity.class);
                        startActivity(b);
                        break;
                    case R.id.nav_profile:
                        break;
                }
                return false;
            }
        });
    }
}
