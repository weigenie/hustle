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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Timer;

public class StatsActivity extends AppCompatActivity {

    TextView text;
    static Long elapsed;
    BottomNavigationView navigation;
    DatabaseReference ref;
    FirebaseAuth auth;
    User user;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        initValues();
        initListeners();

        render();
    }

    private void initValues() {
        elapsed = Long.valueOf(-1);
        text =  (TextView) findViewById(R.id.text_durationDisplay);
        navigation = (BottomNavigationView) findViewById(R.id.bottom_nav);
        auth = FirebaseAuth.getInstance();
        ref = FirebaseDatabase.getInstance().getReference("users");
        user = new User(-1);
    }

    private void initListeners() {
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

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    user = dataSnapshot.child(auth.getUid()).child("timer").getValue(User.class);
                    render();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println(databaseError.getMessage());
            }
        });
    }

    private void render() {
        elapsed = user.duration;
        long hours = elapsed.longValue() / 60;
        long minutes = elapsed.longValue() - hours * 60;
        text.setText(String.format("%dH %02dMINS", hours, minutes));
    }
}
