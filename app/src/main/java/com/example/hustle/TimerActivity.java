package com.example.hustle;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class TimerActivity extends AppCompatActivity implements View.OnClickListener {

    private String TAG = "TimerActivity";
    long timeCountInMS = 60000;

    enum TimerStatus {
        START,
        STOP
    }
    TimerStatus timerStatus = TimerStatus.STOP;

    ProgressBar progressBarCircle;
    EditText editTextMinute;
    TextView textViewTime;

    //these imageviews act as buttons now!
    ImageView imageViewStartStop;
    ImageView imageViewReset;
    CountDownTimer countDownTimer;

    /*Pop-up dialog before starting timer*/
    Button buttonCloseMsg;
    Dialog startTimerDialog;
    TextView startTimerBodyMsg, startTimerHeaderMsg;

    /*Pop-up dialog when timer ends*/
    Button endButtonCloseMsg;
    Dialog endTimerDialog;
    TextView endTimerBodyMsg, endTimerHeaderMsg;

    /*Pop-up dialog when timer is paused*/
    Button confirmResetButton;
    Button cancelResetButton;
    Dialog pauseTimerDialog;
    TextView pauseTimerBodyMsg, pauseTimerHeaderMsg;

    /*Variables needed when timer ends*/
    Vibrator vibrator;

    long timeElapsed = 0;
    String date;
    BottomNavigationView bottomNavigation;
    long totalTime;
    long loggedTime;

    DatabaseReference dbRef;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        initValues();
        initListeners();
    }

    private void initValues() {
        auth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("users")
                .child(auth.getUid()).child("timer");
        bottomNavigation = (BottomNavigationView) findViewById(R.id.bottom_nav);
        date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());

        progressBarCircle = (ProgressBar) findViewById(R.id.progressBarCircle);
        editTextMinute = (EditText) findViewById(R.id.editTextMinute);
        textViewTime = (TextView) findViewById(R.id.text_duration);
        imageViewStartStop = (ImageView) findViewById(R.id.imageViewStartStop);
        imageViewReset = (ImageView) findViewById(R.id.imageViewReset);

        /*Dialog box*/
        startTimerDialog = new Dialog(this);
        endTimerDialog = new Dialog(this);
        pauseTimerDialog = new Dialog(this);

        /*Vibrator*/
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    }

    private void initListeners () {
        //make the play and pause buttons clickable
        imageViewStartStop.setOnClickListener(this);

        //Bottom Navigation Bar navigation
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
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
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    totalTime = dataSnapshot.child("total").getValue(Long.class);
                    loggedTime = dataSnapshot.child(date).getValue(Long.class);
                    System.out.println("loggedTime current duration: " + loggedTime);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    dbRef.child(date).setValue(0);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println(databaseError.getMessage());
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imageViewReset:
                reset();
                break;
            case R.id.imageViewStartStop:
                startStop();
                break;
        }
    }

    //can tentatively ignore this and all the reset features
    private void reset() {
        stopTimer();
        startTimer();
    }

    //method to start and stop the timer
    private void startStop() {
        if (timerStatus == TimerStatus.STOP) {

            /*Dialog Box*/
            ShowStartTimerMsg();
            setTimerValues();
            setProgressBarValues();
            imageViewStartStop.setImageResource(R.drawable.pause);
            editTextMinute.setEnabled(false);
            timerStatus = TimerStatus.START;
        } else {
            ShowPauseTimerMsg();
            imageViewReset.setVisibility(View.GONE);
            imageViewStartStop.setImageResource(R.drawable.play);
            editTextMinute.setEnabled(true);
            timerStatus = TimerStatus.STOP;
            //stopTimer();
        }
    }

    //initialize the input timer value
    private void setTimerValues () {
        int time = 0;
        String num = editTextMinute.getText().toString();

        if (!num.isEmpty()) {
            time = Integer.parseInt(num.trim());
        } else {
            Toast.makeText(getApplicationContext(), "Enter minutes please", Toast.LENGTH_LONG).show();
        }
        timeCountInMS = time*60*1000;
    }

    //start the timer
    private void startTimer() {
        countDownTimer = new CountDownTimer(timeCountInMS, 1000) {
            @Override
            public void onTick(long msUntilDone) {
                textViewTime.setText(hmsTimeFormatter(msUntilDone));
                progressBarCircle.setProgress((int) (msUntilDone / 1000));
                timeElapsed++;
                Log.i(TAG, "incrementing time: " + timeElapsed);
            }

            @Override
            public void onFinish() {

                /*Vibration*/
                if (Build.VERSION.SDK_INT >= 26) {
                    vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(200);
                }

                ShowEndTimerMsg();
                textViewTime.setText(hmsTimeFormatter(timeCountInMS));
                setProgressBarValues();
                imageViewReset.setVisibility(View.GONE);
                imageViewStartStop.setImageResource(R.drawable.play);
                editTextMinute.setEnabled(true);
                timerStatus = TimerStatus.STOP;
            }
        }.start();
        countDownTimer.start();
    }

    //stopping the timer just cancels the timer and resets it immediately
    private void stopTimer() {
        countDownTimer.cancel();
    }

    //progress bar animation
    private void setProgressBarValues() {
        progressBarCircle.setMax((int) timeCountInMS / 1000);
        progressBarCircle.setProgress((int) timeCountInMS / 1000);
    }

    //copied the code but this is hour, minute, second time formatter
    private String hmsTimeFormatter(long milliSeconds) {
        String hms = String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(milliSeconds),
                TimeUnit.MILLISECONDS.toMinutes(milliSeconds) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliSeconds)),
                TimeUnit.MILLISECONDS.toSeconds(milliSeconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliSeconds)));
        return hms;
    }

    private void ShowStartTimerMsg() {
        startTimerDialog.setContentView(R.layout.timer_start_popup);
        buttonCloseMsg = (Button) startTimerDialog.findViewById(R.id.button_gotit);
        startTimerHeaderMsg = (TextView) startTimerDialog.findViewById(R.id.ready_textView);
        startTimerBodyMsg = (TextView) startTimerDialog.findViewById(R.id.startTimerMsg_textView);

        buttonCloseMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTimerDialog.dismiss();
                startTimer();
            }
        });

        startTimerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        startTimerDialog.show();
    }

    private void ShowEndTimerMsg() {
        endTimerDialog.setContentView(R.layout.timer_end_popup);
        endButtonCloseMsg = (Button) endTimerDialog.findViewById(R.id.button_gotcha);
        endTimerHeaderMsg = (TextView) endTimerDialog.findViewById(R.id.endTimerHeader_textView);
        endTimerBodyMsg = (TextView) endTimerDialog.findViewById(R.id.endTimerMsg_textView);

        endButtonCloseMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endTimerDialog.dismiss();
                handleElapsedTime();
            }
        });

        endTimerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        endTimerDialog.show();
    }

    private void ShowPauseTimerMsg() {
        pauseTimerDialog.setContentView(R.layout.timer_pause_popup);
        confirmResetButton = (Button) pauseTimerDialog.findViewById(R.id.button_confirm);
        cancelResetButton = (Button) pauseTimerDialog.findViewById(R.id.button_cancel);
        pauseTimerHeaderMsg = (TextView) pauseTimerDialog.findViewById(R.id.pauseTimerHeader_textView);
        pauseTimerBodyMsg = (TextView) pauseTimerDialog.findViewById(R.id.pauseTimerMsg_textView);

        confirmResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pauseTimerDialog.dismiss();
                stopTimer();
                textViewTime.setText(hmsTimeFormatter(timeCountInMS));
                setProgressBarValues();
                imageViewReset.setVisibility(View.GONE);
                imageViewStartStop.setImageResource(R.drawable.play);
                editTextMinute.setEnabled(true);
                timerStatus = TimerStatus.STOP;
                handleElapsedTime();
            }
        });

        cancelResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pauseTimerDialog.dismiss();
            }
        });

        pauseTimerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        pauseTimerDialog.show();
    }

    private void handleElapsedTime() {
        Log.i(TAG, "handleElapsedTime: adding " + timeElapsed);
        dbRef.child(date).setValue(loggedTime + timeElapsed / 2);
        dbRef.child("total").setValue(totalTime + timeElapsed / 2);
        timeElapsed = 0;
    }
}