package com.example.hustle;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ShopActivity extends AppCompatActivity {

    private String TAG = "ShopActivity";

    Long coins_amt, bear_amt, duck_amt, dog_amt, deer_amt;
    TextView txt_coins;
    ImageView bear, duck, dog, deer;
    TextView txt_bear, txt_duck, txt_dog, txt_deer;

    FirebaseAuth auth;
    DatabaseReference coinsRef;
    DatabaseReference charsRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);
        initValues();
        initListeners();
    }

    private void initValues() {
        coins_amt = Long.valueOf(0);
        txt_coins = (TextView) findViewById(R.id.total_coins);
        bear = (ImageView) findViewById(R.id.bear_image);
        duck = (ImageView) findViewById(R.id.duck_image);
        dog = (ImageView) findViewById(R.id.dog_image);
        deer = (ImageView) findViewById(R.id.deer_image);
        txt_bear = (TextView) findViewById(R.id.bear_amt);
        txt_duck = (TextView) findViewById(R.id.duck_amt);
        txt_dog = (TextView) findViewById(R.id.dog_amt);
        txt_deer = (TextView) findViewById(R.id.deer_amt);

        auth = FirebaseAuth.getInstance();
        coinsRef = FirebaseDatabase.getInstance().getReference("users").child(auth.getUid())
                .child("coins");
        charsRef = FirebaseDatabase.getInstance().getReference("users").child(auth.getUid())
                .child("characters");
    }

    private void initListeners() {
        coinsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                coins_amt = dataSnapshot.getValue(Long.class);
                render();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "coinsRef onCancelled called", databaseError.toException());
            }
        });

        charsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                bear_amt = dataSnapshot.child("bear").getValue(Long.class);
                duck_amt = dataSnapshot.child("duck").getValue(Long.class);
                dog_amt = dataSnapshot.child("dog").getValue(Long.class);
                deer_amt = dataSnapshot.child("deer").getValue(Long.class);
                render();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "charsRef onCancelled called", databaseError.toException());
            }
        });

        bear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (coins_amt >= 500) {
                    coins_amt -= 500;
                    coinsRef.setValue(coins_amt);
                    bear_amt++;
                    charsRef.child("bear").setValue(bear_amt);
                }
            }
        });

        duck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (coins_amt >= 500) {
                    coins_amt -= 500;
                    coinsRef.setValue(coins_amt);
                    duck_amt++;
                    charsRef.child("duck").setValue(duck_amt);
                }
            }
        });

        dog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (coins_amt >= 500) {
                    coins_amt -= 500;
                    coinsRef.setValue(coins_amt);
                    dog_amt++;
                    charsRef.child("dog").setValue(dog_amt);
                }
            }
        });

        deer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (coins_amt >= 500) {
                    coins_amt -= 500;
                    coinsRef.setValue(coins_amt);
                    deer_amt++;
                    charsRef.child("deer").setValue(deer_amt);
                }
            }
        });
    }

    private void render() {
        if (bear_amt == null) {
            bear_amt = Long.valueOf(0);
        }
        if (duck_amt == null) {
            duck_amt = Long.valueOf(0);
        }
        if (dog_amt == null) {
            dog_amt = Long.valueOf(0);
        }
        if (deer_amt == null) {
            deer_amt = Long.valueOf(0);
        }
        txt_coins.setText(coins_amt.toString());
        txt_bear.setText(bear_amt.toString());
        txt_duck.setText(duck_amt.toString());
        txt_dog.setText(dog_amt.toString());
        txt_deer.setText(deer_amt.toString());
    }
}
