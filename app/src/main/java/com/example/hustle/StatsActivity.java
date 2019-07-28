package com.example.hustle;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
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

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class StatsActivity extends AppCompatActivity {

    private String TAG = "StatsActivity";
    TextView txt_my_total, txt_my_today, txt_all_total, txt_all_today, txt_all_avg;
    Long my_total, my_today, all_total, all_today, total_users;
    String date;
    BottomNavigationView navigation;
    DatabaseReference ref;
    DatabaseReference statsRef;
    FirebaseAuth auth;
    ImageButton logOut;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        initValues();
        initListeners();
    }

    private void initValues() {
        my_today = Long.valueOf(0);
        my_total= Long.valueOf(0);
        all_total= Long.valueOf(0);
        all_today= Long.valueOf(0);
        total_users = Long.valueOf(1);
        txt_my_total =  (TextView) findViewById(R.id.txt_my_total);
        txt_my_today = (TextView) findViewById(R.id.txt_my_today);
        txt_all_total = (TextView) findViewById(R.id.txt_all_total);
        txt_all_today = (TextView) findViewById(R.id.txt_all_today);
        txt_all_avg = (TextView) findViewById(R.id.txt_all_avg);
        navigation = (BottomNavigationView) findViewById(R.id.bottom_nav);
        logOut = (ImageButton) findViewById(R.id.btn_signout);
        date = ZonedDateTime.now(ZoneId.of("UTC+08:00")).toLocalDate().toString();

        auth = FirebaseAuth.getInstance();
        ref = FirebaseDatabase.getInstance().getReference("users").child(auth.getUid())
                .child("timer");
        statsRef = FirebaseDatabase.getInstance().getReference("analytics");
    }

    private void initListeners() {
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_timer:
                        Intent a = new Intent(StatsActivity.this,TimerActivity.class);
                        startActivity(a);
                        StatsActivity.this.finish();
                        break;
                    case R.id.nav_todo:
                        Intent b = new Intent(StatsActivity.this,TodoActivity.class);
                        startActivity(b);
                        StatsActivity.this.finish();
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
                    my_total = dataSnapshot.child("total").getValue(Long.class);
                    my_today = dataSnapshot.child(date).getValue(Long.class);
                    Log.i(TAG, "ref added: \nmy_total: " + my_total +
                            "\nmy_today: " + my_today);
                    render();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "ref onCancelled called", databaseError.toException());
            }
        });

        statsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                all_today = dataSnapshot.child(date).getValue(Long.class);
                all_total = dataSnapshot.child("total").getValue(Long.class);
                total_users = dataSnapshot.child("total_users").getValue(Long.class);
                Log.i(TAG, "statsRef added: " +
                        "\nall_today: " + all_today +
                        "\nall_total: " + all_total +
                        "\ntotal_users: " + total_users);
                render();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "statsRef onCancelled called", databaseError.toException());
            }
        });

        logOut.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                StatsActivity.this.logOut();
            }
        });

    }

    private String processTime(Long seconds) {
        long totalMin = seconds / 60;
        long hours = totalMin / 60;
        long minutes = totalMin - hours * 60;
        Log.i("processTime", "seconds: " + seconds +
                "\nhours: " + hours +
                "\nminutes: " + minutes);
        return String.format("%dH %02dMINS", hours, minutes);
    }

    private void render() {
        txt_my_total.setText(processTime(my_total));
        txt_my_today.setText(processTime(my_today));
        txt_all_total.setText(processTime(all_total));
        txt_all_today.setText(processTime(all_today));
        if (!total_users.equals(Long.valueOf(0))) {
            txt_all_avg.setText(processTime(all_today / total_users));
        } else {
            txt_all_avg.setText("-1");
        }
    }

    private void logOut() {
        auth.signOut();
        Intent i = new Intent(StatsActivity.this, LoginActivity.class);
        startActivity(i);
        this.finish();
    }
}
