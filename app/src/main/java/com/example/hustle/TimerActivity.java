package com.example.hustle;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class TimerActivity extends AppCompatActivity {
//
    long duration; // in ms
    static long totalTimeElapsed;
    long timeElapsed = 0;
    Button button_start;
    TextView timerDuration;
    boolean isTicking;
    CountDownTimer timer;
//
////    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
//
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        duration = 1510000;
        totalTimeElapsed = 10000;
        button_start = (Button) findViewById(R.id.button_timer);
        timerDuration = (TextView) findViewById(R.id.text_duration);
        isTicking = false;
        render();

        button_start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                try {
                    if (!isTicking) {
                        startTimer();
                    } else {
                        stopTimer();
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        });

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.bottom_nav);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_timer:
                        break;
                    case R.id.nav_todo:
                        Intent a = new Intent(TimerActivity.this,TodoActivity.class);
                        startActivity(a);
                        break;
                    case R.id.nav_profile:
                        Intent b = new Intent(TimerActivity.this,StatsActivity.class);
                        startActivity(b);
                        break;
                }
                return false;
            }
        });
    }

    private void startTimer() {
        button_start.setBackgroundResource(R.drawable.pause);
        timer = new CountDownTimer(Long.valueOf(duration), 1000) {
            @Override
            public void onTick(long l) {
                System.out.println("duration: " + duration);
                duration -= 1000;
                render();
            }

            @Override
            public void onFinish() {
                Toast.makeText(getApplicationContext(), "Done!!", Toast.LENGTH_SHORT).show();
            }
        }.start();
        isTicking = true;
        Toast.makeText(getApplicationContext(), "Timer started", Toast.LENGTH_SHORT).show();
    }

    private void stopTimer() {
        button_start.setBackgroundResource(R.drawable.play);
        try {
            timer.cancel();
            Toast.makeText(getApplicationContext(), "Timer stopped", Toast.LENGTH_SHORT).show();
            isTicking = false;
            handleElapsedTime();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void render() {
        int seconds = (int) duration / 1000;
        int minutes = seconds / 60;
        seconds = seconds % 60;
        timerDuration.setText(String.format("%d:%02d", minutes, seconds));
    }

    private void handleElapsedTime() {
        totalTimeElapsed += timeElapsed;
        timeElapsed = 0;
    }
}

