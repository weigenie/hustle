package com.example.hustle;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class TodoActivity extends AppCompatActivity {

        private static final String TAG = "MainActivity";
        private TaskDbHelper mHelper;
        private ListView mTaskListView;
        private ArrayAdapter<String> mAdapter;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_todos);

            mHelper = new TaskDbHelper(this);
            SQLiteDatabase db = mHelper.getReadableDatabase();
            mTaskListView = (ListView) findViewById(R.id.list_todo);

            Cursor cursor = db.query(TaskContract.TaskEntry.TABLE,
                    new String[]{TaskContract.TaskEntry._ID, TaskContract.TaskEntry.COL_TASK_TITLE},
                    null, null, null, null, null);
            while(cursor.moveToNext()) {
                int idx = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_TITLE);
                Log.d(TAG, "Task: " + cursor.getString(idx));
            }
            cursor.close();
            db.close();
            updateUI();

            BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.bottom_nav);
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
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            getMenuInflater().inflate(R.menu.main_menu, menu);
            return super.onCreateOptionsMenu(menu);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_add_task:
                    final EditText taskEditText = new EditText(this);
                    AlertDialog dialog = new AlertDialog.Builder(this)
                            .setTitle("Add a new task")
                            .setMessage("What do you want to do next?")
                            .setView(taskEditText)
                            .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String task = String.valueOf(taskEditText.getText());
                                    SQLiteDatabase db = mHelper.getWritableDatabase();
                                    ContentValues values = new ContentValues();
                                    values.put(TaskContract.TaskEntry.COL_TASK_TITLE, task);
                                    db.insertWithOnConflict(TaskContract.TaskEntry.TABLE,
                                            null,
                                            values,
                                            SQLiteDatabase.CONFLICT_REPLACE);
                                    db.close();
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .create();
                    dialog.show();
                    updateUI();
                    return true;

                default:
                    return super.onOptionsItemSelected(item);
            }
        }

        private void updateUI() {
            ArrayList<String> taskList = new ArrayList<>();
            SQLiteDatabase db = mHelper.getReadableDatabase();
            Cursor cursor = db.query(TaskContract.TaskEntry.TABLE,
                    new String[]{TaskContract.TaskEntry._ID, TaskContract.TaskEntry.COL_TASK_TITLE},
                    null, null, null, null, null);
            while (cursor.moveToNext()) {
                int idx = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_TITLE);
                taskList.add(cursor.getString(idx));
            }

            if (mAdapter == null) {
                mAdapter = new ArrayAdapter<>(this,
                        R.layout.item_do,
                        R.id.task_title,
                        taskList);
                mTaskListView.setAdapter(mAdapter);
            } else {
                mAdapter.clear();
                mAdapter.addAll(taskList);
                mAdapter.notifyDataSetChanged();
            }

            cursor.close();
            db.close();
        }
    }


//    TextView titlepage, subtitlepage, endpage;
//
//    RecyclerView ourdoes;
//    ArrayList<MyDo> list;
//    DoAdapter doAdapter;
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_todos);
//
//        ourdoes = findViewById(R.id.ourdoes);
//        ourdoes.setLayoutManager(new LinearLayoutManager(this));
//        list = new ArrayList<MyDo>();
//    }
//    EditText txt;
//    Button btn_add;
//    ListView list;
//    ScrollView test;
//
//    private ArrayList<String> todos = new ArrayList<>();
//    private ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_does, todos);
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_todo);
//
//        btn_add = (Button) findViewById(R.id.addTodo);
//        txt = (EditText) findViewById(R.id.todoText);
//        list = (ListView) findViewById(R.id.todos);
//
//        list.setAdapter(adapter);
//        todos.add("First Item");
//        todos.add("Second Item");

//        btn_add.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                String todo = txt.getText().toString();
//                txt.setText("");
//                Toast.makeText(getApplicationContext(), "Todo added", Toast.LENGTH_SHORT).show();
//            }
//        });
//      }
