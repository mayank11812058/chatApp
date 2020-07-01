package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    Toolbar toolbar;
    ImageView imageView;
    AppCompatButton loginButton;
    Button signUpButton;
    TextView errorTextView;
    TextInputLayout emailLayout;
    TextInputLayout passwordLayout;
    AppCompatEditText emailEditText;
    AppCompatEditText passwordEditText;
    ProgressBar progressBar;
    FirebaseAuth firebaseAuth;
    FirebaseAuth.AuthStateListener authStateListener;
    FirebaseUser firebaseUser;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference userReference = db.collection("User");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        toolbar = findViewById(R.id.loginToolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(this);
        signUpButton = findViewById(R.id.signUpButton);
        signUpButton.setOnClickListener(this);
        imageView = findViewById(R.id.loginImageView);
        progressBar = findViewById(R.id.progressBar);
        errorTextView = findViewById(R.id.loginErrorTextView);
        emailLayout = findViewById(R.id.loginEmailLayout);
        passwordLayout = findViewById(R.id.loginPasswordLayout);
        emailEditText = findViewById(R.id.loginEmailText);
        passwordEditText = findViewById(R.id.loginPasswordText);
        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                firebaseUser = firebaseAuth.getCurrentUser();

                if (firebaseUser != null) {

                } else {

                }
            }
        };

        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 8) {
                    passwordLayout.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });


        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isEmailValid(s.toString())) {
                    emailLayout.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.loginButton:
                login();
                break;
            case R.id.signUpButton:
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
                finish();
                break;
        }
    }

    public boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public void login() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if(!isEmailValid(email)){
            emailLayout.setErrorEnabled(true);
            emailLayout.setError("Must be valid email address");
        }

        if(password.length() < 8){
            passwordLayout.setErrorEnabled(true);
            passwordLayout.setError("Password must ne of minimum 8 characters");
        }

        if (!emailLayout.isErrorEnabled() && !passwordLayout.isErrorEnabled()) {
            progressBar.setVisibility(View.VISIBLE);
            errorTextView.setVisibility(View.GONE);

            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                firebaseUser = firebaseAuth.getCurrentUser();
                                userReference.whereEqualTo("userId", firebaseUser.getUid().toString())
                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                            if(e == null){
                                                for(QueryDocumentSnapshot snapshots : queryDocumentSnapshots){
                                                    CurrentUser currentUser = new CurrentUser();
                                                    currentUser.setUsername(snapshots.getString("name"));
                                                    currentUser.setEmail(snapshots.getString("email"));
                                                    currentUser.setUserId(snapshots.getString("userId"));
                                                    currentUser.setImageUri(snapshots.getString("imageUri"));
                                                }
                                                Intent intent = new Intent(LoginActivity.this, ChatsActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }else{
                                                progressBar.setVisibility(View.GONE);
                                                errorTextView.setVisibility(View.VISIBLE);
                                                errorTextView.setText("Something wrong occured");
                                            }
                                        }
                                    });
                            }else{
                                progressBar.setVisibility(View.GONE);
                                errorTextView.setVisibility(View.VISIBLE);
                                errorTextView.setText(task.getResult().toString());
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    String message = e.getMessage();
                    errorTextView.setText(message);
                    errorTextView.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    }
}