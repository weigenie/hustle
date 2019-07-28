package com.example.hustle;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class TodoActivity extends AppCompatActivity {

    private String TAG = "TodoActivity";
    private FloatingActionButton add;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    static ArrayList<Todo> todos;
    BottomNavigationView navigation;
    static DatabaseReference fireDb;
    FirebaseAuth auth;
    ChildEventListener childEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo);

        initValues();
        initListeners();
    }

    private void initValues() {
        add = (FloatingActionButton) findViewById(R.id.fab_add);
        navigation = (BottomNavigationView) findViewById(R.id.bottom_nav);

        todos = new ArrayList<>();
        auth = FirebaseAuth.getInstance();
        fireDb = FirebaseDatabase.getInstance().getReference("users")
                .child(auth.getUid()).child("todos");
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String newTodo = dataSnapshot.getValue(String.class);
                String newID = dataSnapshot.getKey();
                Log.i(TAG, "adding child...\ntxt_my_total: " + newTodo + "\nID: " + newID);
                todos.add(0, new Todo(newID, newTodo));
                TodoActivity.this.updateUI();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                System.out.println("child event changed");
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                System.out.println("child event removed");
                String removed_ID = dataSnapshot.getKey();
                String removed_text = dataSnapshot.getValue(String.class);
                Log.i(TAG, "removed: " + removed_text);
                todos.remove(new Todo(removed_ID, removed_text));
                TodoActivity.this.updateUI();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.i(TAG, "child event moved");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.i(TAG, "child event cancelled");
            }
        };

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView_todo);
        layoutManager = new LinearLayoutManager(getApplicationContext());
        mAdapter = new MyAdapter(todos);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new MyDividerItemDecoration(this,
                LinearLayoutManager.VERTICAL, 16));
        recyclerView.setAdapter(mAdapter);
    }

    private void initListeners() {
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_timer:
                        Intent a = new Intent(TodoActivity.this,TimerActivity.class);
                        startActivity(a);
                        TodoActivity.this.finish();
                        break;
                    case R.id.nav_todo:
                        break;
                    case R.id.nav_profile:
                        Intent b = new Intent(TodoActivity.this,StatsActivity.class);
                        startActivity(b);
                        TodoActivity.this.finish();
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
                    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            System.out.println("dialog dismissed");
                            updateUI();
                        }
                    });
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        });

        fireDb.addChildEventListener(childEventListener);
    }

    private void updateUI() {
        mAdapter.notifyDataSetChanged();
    }
}

class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private ArrayList<Todo> mDataset;
    private String TAG = "MyAdapter";

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        Button delete;
        MyViewHolder(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.task_title);
            delete = (Button) v.findViewById(R.id.task_delete);
        }
    }

    public MyAdapter(ArrayList<Todo> myDataset) {
        this.mDataset = myDataset;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = (View) LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_do, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        holder.title.setText(mDataset.get(position).text);
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "onClick");
                String ID = mDataset.get(position).ID;
                DatabaseReference db = TodoActivity.fireDb;
                db.child(ID).removeValue();
                MyAdapter.this.notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}

class MyDividerItemDecoration extends RecyclerView.ItemDecoration {
    private static final int[] ATTRS = new int[]{
            android.R.attr.listDivider
    };

    public static final int HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL;

    public static final int VERTICAL_LIST = LinearLayoutManager.VERTICAL;

    private Drawable mDivider;
    private int mOrientation;
    private Context context;
    private int margin;

    public MyDividerItemDecoration(Context context, int orientation, int margin) {
        this.context = context;
        this.margin = margin;
        final TypedArray a = context.obtainStyledAttributes(ATTRS);
        mDivider = a.getDrawable(0);
        a.recycle();
        setOrientation(orientation);
    }

    public void setOrientation(int orientation) {
        if (orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST) {
            throw new IllegalArgumentException("invalid orientation");
        }
        mOrientation = orientation;
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (mOrientation == VERTICAL_LIST) {
            drawVertical(c, parent);
        } else {
            drawHorizontal(c, parent);
        }
    }

    public void drawVertical(Canvas c, RecyclerView parent) {
        final int left = parent.getPaddingLeft();
        final int right = parent.getWidth() - parent.getPaddingRight();

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                    .getLayoutParams();
            final int top = child.getBottom() + params.bottomMargin;
            final int bottom = top + mDivider.getIntrinsicHeight();
            mDivider.setBounds(left + dpToPx(margin), top, right - dpToPx(margin), bottom);
            mDivider.draw(c);
        }
    }

    public void drawHorizontal(Canvas c, RecyclerView parent) {
        final int top = parent.getPaddingTop();
        final int bottom = parent.getHeight() - parent.getPaddingBottom();

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                    .getLayoutParams();
            final int left = child.getRight() + params.rightMargin;
            final int right = left + mDivider.getIntrinsicHeight();
            mDivider.setBounds(left, top + dpToPx(margin), right, bottom - dpToPx(margin));
            mDivider.draw(c);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (mOrientation == VERTICAL_LIST) {
            outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
        } else {
            outRect.set(0, 0, mDivider.getIntrinsicWidth(), 0);
        }
    }

    private int dpToPx(int dp) {
        Resources r = context.getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }
}
