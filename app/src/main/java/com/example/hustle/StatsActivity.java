package com.example.hustle;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Objects;

public class StatsActivity extends AppCompatActivity {

    private String TAG = "StatsActivity";
    TextView txt_my_total, txt_my_today, txt_all_total, txt_all_today, txt_all_avg,
            txt_coins;
    Long my_total, my_today, all_total, all_today, total_users, coins_amt;
    Long[] past7days;
    String date;
    BottomNavigationView navigation;
    DatabaseReference ref;
    DatabaseReference statsRef;
    DatabaseReference coinsRef;
    FirebaseAuth auth;
    ImageButton logOut, toShop;
    GraphView graph;

    /*Pop-up dialog when timer is paused*/
    Button confirmLogoutButton;
    Button cancelLogoutButton;
    Dialog logoutDialog;
    TextView logoutBodyMsg, logoutHeaderMsg;

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
        coins_amt = Long.valueOf(0);
        past7days = new Long[7];
        Arrays.fill(past7days, Long.valueOf(0));
        graph = (GraphView) findViewById(R.id.stats_graph);
        txt_my_total =  (TextView) findViewById(R.id.txt_my_total);
        txt_my_today = (TextView) findViewById(R.id.txt_my_today);
        txt_all_total = (TextView) findViewById(R.id.txt_all_total);
        txt_all_today = (TextView) findViewById(R.id.txt_all_today);
        txt_all_avg = (TextView) findViewById(R.id.txt_all_avg);
        txt_coins = (TextView) findViewById(R.id.coins_textview);
        navigation = (BottomNavigationView) findViewById(R.id.bottom_nav);
        logOut = (ImageButton) findViewById(R.id.btn_signout);
        toShop = (ImageButton) findViewById(R.id.btn_toShop);
        date = ZonedDateTime.now(ZoneId.of("UTC+08:00")).toLocalDate().toString();

        /*Dialog box*/
        logoutDialog = new Dialog(this);

        auth = FirebaseAuth.getInstance();
        ref = FirebaseDatabase.getInstance().getReference("users").child(auth.getUid())
                .child("timer");
        statsRef = FirebaseDatabase.getInstance().getReference("analytics");
        coinsRef = FirebaseDatabase.getInstance().getReference("users")
                    .child(auth.getUid()).child("coins");
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
                    for (int i = 0; i < 7; i++) {
                        String otherDate = ZonedDateTime.now(ZoneId.of("UTC+08:00")).minusDays(6 - i)
                                            .toLocalDate().toString();
                        past7days[i] = dataSnapshot.child(otherDate).getValue(Long.class);
                        if (past7days[i] == null) {
                            past7days[i] = 0L;
                        }
                    }
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
                ShowLogOutDialog();
            }
        });

        coinsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                coins_amt = dataSnapshot.getValue(Long.class);
                render();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.i(TAG, "coinsRef onCancelled called");
            }
        });

        toShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(StatsActivity.this, ShopActivity.class);
                startActivity(i);
                StatsActivity.this.finish();
            }
        });
    }

    private void ShowLogOutDialog() {
        logoutDialog.setContentView(R.layout.logout_popup);
        confirmLogoutButton = (Button) logoutDialog.findViewById(R.id.logout_button_confirm);
        cancelLogoutButton = (Button) logoutDialog.findViewById(R.id.logout_button_cancel);
        logoutHeaderMsg = (TextView) logoutDialog.findViewById(R.id.logout_header_textView);
        logoutBodyMsg = (TextView) logoutDialog.findViewById(R.id.logout_body_textView);

        cancelLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoutDialog.dismiss();
            }
        });

        confirmLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StatsActivity.this.logOut();
            }
        });

        logoutDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        logoutDialog.show();
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
        if (my_today == null) my_today = Long.valueOf(0);
        if (my_total == null) my_total= Long.valueOf(0);
        if (all_total == null) all_total = Long.valueOf(0);
        if (all_today == null) all_today= Long.valueOf(0);
        if (coins_amt == null) coins_amt = Long.valueOf(0);
        txt_my_total.setText(processTime(my_total));
        txt_my_today.setText(processTime(my_today));
        txt_all_total.setText(processTime(all_total));
        txt_all_today.setText(processTime(all_today));
        txt_coins.setText(coins_amt.toString());
        if (!total_users.equals(Long.valueOf(0))) {
            txt_all_avg.setText(processTime(all_today / total_users));
        } else {
            txt_all_avg.setText("-1");
        }
        graph.setVisibility(View.VISIBLE);
        try {
            LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[] {
                    new DataPoint(0, past7days[0]),
                    new DataPoint(1, past7days[1]),
                    new DataPoint(2, past7days[2]),
                    new DataPoint(3, past7days[3]),
                    new DataPoint(4, past7days[4]),
                    new DataPoint(5, past7days[5]),
                    new DataPoint(6, past7days[6]),
            });
            graph.addSeries(series);
        } catch (IllegalArgumentException e) {
            Log.i(TAG, Objects.requireNonNull(e.getMessage()));
        }
    }

    private void logOut() {
        auth.signOut();
        Intent i = new Intent(StatsActivity.this, LoginActivity.class);
        startActivity(i);
        this.finish();
    }
}
