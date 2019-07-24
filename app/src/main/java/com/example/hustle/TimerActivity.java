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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TimerActivity extends AppCompatActivity {

    long duration;
    long timeElapsed = 0;
    boolean isTicking;
    Button button_start;
    TextView timerDuration;
    CountDownTimer timer;
    BottomNavigationView navigation;
    DatabaseReference dbRef;
    FirebaseAuth auth;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        initValues();
        initListeners();

        render();
    }

    private void initValues() {
        duration = 1500000;
        button_start = (Button) findViewById(R.id.button_timer);
        timerDuration = (TextView) findViewById(R.id.text_duration);
        isTicking = false;
        navigation = (BottomNavigationView) findViewById(R.id.bottom_nav);

        auth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("users");
        user = new User(-1);
    }

    private void initListeners() {
        button_start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (!isTicking) {
                    startTimer();
                } else {
                    stopTimer();
                }
            }
        });

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

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    user = dataSnapshot.child(auth.getUid()).child("timer").getValue(User.class);
                    System.out.println("user current duration: " + user.duration);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    dbRef.child(auth.getUid()).child("timer").setValue(new User(0));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println(databaseError.getMessage());
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
                timeElapsed++;
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
        dbRef.child(auth.getUid()).child("timer").setValue(user.addTime(timeElapsed));
        timeElapsed = 0;
    }
}