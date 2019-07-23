package com.example.hustle;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

public class TodoActivity extends AppCompatActivity {

        private FloatingActionButton add;
        private static final String TAG = "MainActivity";
        private ListView mTaskListView;
        ArrayList<String> todos = new ArrayList<>();
        private ArrayAdapter<String> mAdapter;
        BottomNavigationView navigation;
        DatabaseReference fireDb;
        FirebaseAuth auth;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_todos);

            initValues();
            initListeners();
            updateUI();
        }

        private void initValues() {
            add = (FloatingActionButton) findViewById(R.id.fab_add);
            mTaskListView = (ListView) findViewById(R.id.list_todo);
            fireDb = LoginActivity.db.getReference("users");
            auth = FirebaseAuth.getInstance();
            navigation = (BottomNavigationView) findViewById(R.id.bottom_nav);
        }

        private void initListeners() {
            navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.nav_timer:
                            Intent a = new Intent(TodoActivity.this,TimerActivity.class);
                            startActivity(a);
                            break;
                        case R.id.nav_todo:
                            break;
                        case R.id.nav_profile:
                            Intent b = new Intent(TodoActivity.this,StatsActivity.class);
                            startActivity(b);
                            break;
                    }
                    return false;
                }
            });

            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        final EditText taskEditText = new EditText(TodoActivity.this);
                        AlertDialog dialog = new AlertDialog.Builder(TodoActivity.this)
                                .setTitle("Add a new task")
                                .setMessage("What do you want to do next?")
                                .setView(taskEditText)
                                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String task = String.valueOf(taskEditText.getText());
                                        ArrayList<String> temp = new ArrayList<>();
                                        for (String x: todos) {
                                            temp.add(x);
                                            System.out.println("adding: " + x);
                                        }
                                        System.out.println("before: " + temp);
                                        temp.add(task);
                                        System.out.println("after: " + temp);
                                        fireDb.child(auth.getUid()).child("todos").setValue(temp);
                                        todos = temp;
                                    }
                                })
                                .setNegativeButton("Cancel", null)
                                .create();
                        dialog.show();
                        updateUI();
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            });
        }

        private void updateUI() {
            ArrayList<String> temp = new ArrayList<>();
            // need to add reading from firebase
            for (String x: todos) {
                temp.add(x);
            }

            if (mAdapter == null) {
                mAdapter = new ArrayAdapter<>(this,
                        R.layout.item_do,
                        R.id.task_title,
                        temp);
                mTaskListView.setAdapter(mAdapter);
            } else {
                mAdapter.clear();
                mAdapter.addAll(temp);
                mAdapter.notifyDataSetChanged();
            }
        }
    }
