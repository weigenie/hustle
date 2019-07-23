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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

public class TodoActivity extends AppCompatActivity {

    private FloatingActionButton add;
    private ListView mTaskListView;
    private ArrayAdapter<String> mAdapter;
    ArrayList<String> todos;
    BottomNavigationView navigation;
    DatabaseReference fireDb;
    FirebaseAuth auth;
    User user;
    ChildEventListener childEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todos);

        initValues();
        initListeners();
        updateUI();
    }

    private void initValues() {
        todos = new ArrayList<>();
        add = (FloatingActionButton) findViewById(R.id.fab_add);
        mTaskListView = (ListView) findViewById(R.id.list_todo);
        navigation = (BottomNavigationView) findViewById(R.id.bottom_nav);

        auth = FirebaseAuth.getInstance();
        fireDb = LoginActivity.db.getReference("users").child(auth.getUid()).child("todos");
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                System.out.println("child event added");
                String newTodo = dataSnapshot.getValue(String.class);
                todos.add(newTodo);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                System.out.println("child event changed");
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                System.out.println("child event removed");
                ArrayList<String> temp = new ArrayList<>();
                for (DataSnapshot snapshots: dataSnapshot.getChildren()) {
                    temp.add(snapshots.getValue(String.class));
                }
                todos = temp;
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                System.out.println("child event moved");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("child event cancelled");
            }
        };
        user = new User(-10);
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
                                    fireDb.push().setValue(task);
                                    System.out.println("created new task: " + task);
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

        fireDb.addChildEventListener(childEventListener);
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
