package com.example.hustle;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.login.Login;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity {
    Button button_login, button_register;
    EditText edit_username, edit_pw;
    static FirebaseDatabase db;
    static FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        checkLoggedIn();

        initValues();
        initListeners();
    }

    private void initValues() {
        button_login = (Button) findViewById(R.id.button_login);
        button_register = (Button) findViewById(R.id.button_toRegister);
        edit_username = (EditText) findViewById(R.id.edit_username);
        edit_pw = (EditText) findViewById(R.id.edit_password);
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
    }

    private void initListeners() {
        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text_username = edit_username.getText().toString().trim();
                String text_password = edit_pw.getText().toString().trim();
                validate(text_username, text_password);
            }
        });

        button_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent nextIntent = new Intent(LoginActivity.this,
                        RegisterActivity.class);
                startActivity(nextIntent);
                LoginActivity.this.finish();
            }
        });
    }

    private void checkLoggedIn() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            Intent i = new Intent(LoginActivity.this, TimerActivity.class);
            startActivity(i);
            LoginActivity.this.finish();
        }
    }

    private void validate(String user, String pw) {
        try {
            auth.signInWithEmailAndPassword(user, pw).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Intent nextIntent = new Intent(LoginActivity.this, TimerActivity.class);
                        startActivity(nextIntent);
                        LoginActivity.this.finish();
                    } else {
                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
