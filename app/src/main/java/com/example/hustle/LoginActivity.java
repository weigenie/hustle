package com.example.hustle;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
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
    ProgressBar pBar;
    EditText edit_username, edit_pw;
    View grayOut;
    static FirebaseDatabase db;
    static FirebaseAuth auth;

//    CallbackManager callbackManager;
//    LoginButton mLoginButton;
//    private String TAG = "LoginActivity";
//    private String EMAIL = "email";
//    private String USER_POSTS = "user_posts";
//    private String AUTH_TYPE = "rerequest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        checkLoggedIn();

        initValues();
        initListeners();
    }

    private void initValues() {

//        callbackManager = CallbackManager.Factory.create();
//
//        mLoginButton = findViewById(R.id.fb_login);
//        mLoginButton.setAuthType(AUTH_TYPE);

        pBar = (ProgressBar) findViewById(R.id.progressbar);
        grayOut = (View) findViewById(R.id.fadeBackground);
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
            }
        });

//        mLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
//            @Override
//            public void onSuccess(LoginResult loginResult) {
//                setResult(RESULT_OK);
//                Intent i = new Intent(LoginActivity.this, TimerActivity.class);
//                startActivity(i);
//                LoginActivity.this.finish();
//                finish();
//            }
//
//            @Override
//            public void onCancel() {
//                setResult(RESULT_CANCELED);
//                Log.i(TAG,"fb cancelled");
//                finish();
//            }
//
//            @Override
//            public void onError(FacebookException e) {
//                // Handle exception
//                Log.d(TAG, e.getMessage());
//            }
//        });
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
            pBar.setVisibility(View.VISIBLE);
            grayOut.setVisibility(View.VISIBLE);
            grayOut.animate().alpha(0.5f);
            auth.signInWithEmailAndPassword(user, pw).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Intent nextIntent = new Intent(LoginActivity.this, TimerActivity.class);
                        startActivity(nextIntent);
                        LoginActivity.this.finish();
                    } else {
                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        pBar.setVisibility(View.GONE);
                        grayOut.setVisibility(View.GONE);
                        grayOut.setAlpha(0f);
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
